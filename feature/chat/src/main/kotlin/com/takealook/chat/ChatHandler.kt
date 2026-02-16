package com.takealook.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.takealook.auth.component.AUTHORIZATION_HEADER
import com.takealook.auth.component.JwtTokenProvider
import com.takealook.auth.component.LEGACY_HEADER_STRING
import com.takealook.chat.reaction.ReactionCommand
import com.takealook.chat.ticket.WsTicketService
import com.takealook.domain.chat.message.SaveMessageUseCase
import com.takealook.domain.chat.reaction.AddReactionUseCase
import com.takealook.domain.chat.reaction.RemoveReactionUseCase
import com.takealook.domain.chat.users.GetChatUsersByRoomIdUseCase
import com.takealook.domain.user.GetUserByNameUseCase
import com.takealook.domain.user.profile.GetUserProfileByIdUseCase
import com.takealook.model.ChatMessage
import com.takealook.model.ChatReaction
import com.takealook.model.MessageType
import com.takealook.model.UserChatMessage
import com.takealook.model.UserChatReaction
import com.takealook.model.toUserChatMessage
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Controller
class ChatHandler(
    private val objectMapper: ObjectMapper,
    private val getChatUsersByRoomIdUseCase: GetChatUsersByRoomIdUseCase,
    private val getUserByNameUseCase: GetUserByNameUseCase,
    private val getUserProfileByIdUseCase: GetUserProfileByIdUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val addReactionUseCase: AddReactionUseCase,
    private val removeReactionUseCase: RemoveReactionUseCase,
    private val wsTicketService: WsTicketService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val chatBroadcaster: ChatBroadcaster,
    private val meterRegistry: MeterRegistry,
    @Value("\${ws.allowed-origins:https://takealook.app,http://localhost:3000}")
    private val allowedOriginsConfig: String,
    @Value("\${ws.rate-limit.max-messages-per-minute:60}")
    private val wsMaxMessagesPerMinute: Int,
    @Value("\${ws.rate-limit.window-seconds:60}")
    private val wsRateWindowSeconds: Long,
) : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(ChatHandler::class.java)

    private val rateLimiter = ChatRateLimiter(wsMaxMessagesPerMinute, wsRateWindowSeconds * 1000)

    private val allowedOrigins: Set<String> by lazy {
        allowedOriginsConfig.split(",").map { it.trim() }.toSet()
    }

    override fun handle(session: WebSocketSession): Mono<Void?> = mono {
        val origin = session.handshakeInfo.headers.origin
        if (origin != null && origin !in allowedOrigins) {
            meterRegistry.counter("takealook_ws_connections_total", "outcome", "origin_blocked").increment()
            logger.warn("Rejected connection from unauthorized origin: $origin")
            return@mono session.close(CloseStatus.POLICY_VIOLATION).awaitSingleOrNull()
        }

        val params = parseQueryParams(session.handshakeInfo.uri?.query)
        val roomId = params["roomId"]?.toLongOrNull()
        if (roomId == null) {
            meterRegistry.counter("takealook_ws_connections_total", "outcome", "invalid_room").increment()
            logger.warn("Missing roomId in WebSocket handshake")
            return@mono session.close(CloseStatus.POLICY_VIOLATION).awaitSingleOrNull()
        }

        val authResult = authenticateSession(session.handshakeInfo.headers, params)
        val userId = authResult.getOrElse { reason ->
            meterRegistry.counter("takealook_ws_connections_total", "outcome", "unauthorized").increment()
            logger.warn("WebSocket unauthorized: $reason")
            return@mono session.close(CloseStatus.NOT_ACCEPTABLE).awaitSingleOrNull()
        }

        val userProfile = getUserProfileByIdUseCase(userId) ?: run {
            MDC.remove("userId")
            MDC.remove("requestId")
            logger.error("User not found: $userId")
            return@mono session.close(CloseStatus.BAD_DATA).awaitSingleOrNull()
        }

        MDC.put("userId", userId.toString())
        MDC.put("requestId", session.handshakeInfo.headers.getFirst("X-Request-Id") ?: session.id)

        val isRoomMember = getChatUsersByRoomIdUseCase(roomId).any { it.userId == userId }
        if (!isRoomMember) {
            meterRegistry.counter("takealook_ws_connections_total", "outcome", "forbidden").increment()
            logger.warn("User $userId is not a room member of room $roomId")
            MDC.remove("userId")
            MDC.remove("requestId")
            return@mono session.close(CloseStatus.NOT_ACCEPTABLE).awaitSingleOrNull()
        }

        val isFirstSession = chatBroadcaster.attachSession(userId, session)

        meterRegistry.counter("takealook_ws_connections_total", "outcome", "success").increment()
        logger.info("WebSocket session established for user $userId, Room: $roomId, First: $isFirstSession")

        if (isFirstSession) {
            broadcastSystemMessage(roomId, userId, MessageType.JOIN)
        }

        val incoming = session.receive()
            .map { it.payloadAsText }
            .flatMap { rawMessage ->
                mono {
                    try {
                        meterRegistry.counter(
                            "takealook_chat_messages_total",
                            "scope",
                            "ws",
                            "type",
                            detectIncomingType(rawMessage).name.lowercase(),
                            "outcome",
                            "attempt"
                        ).increment()

                        if (!rateLimiter.allow(userId)) {
                            meterRegistry.counter("takealook_ws_rate_limited_total", "user", userId.toString()).increment()
                            logger.warn("Rate limit exceeded for user $userId. Closing connection.")
                            session.close(CloseStatus.POLICY_VIOLATION).awaitSingleOrNull()
                            return@mono null
                        }

                        val type = detectIncomingType(rawMessage)
                        when (type) {
                            MessageType.REACTION -> {
                                val command = objectMapper.readValue<ReactionCommand>(rawMessage)
                                handleReactionCommand(command, roomId, userId)
                            }
                            MessageType.JOIN,
                            MessageType.LEAVE,
                            MessageType.CHAT -> {
                                val chatMessage = objectMapper.readValue<ChatMessage>(rawMessage)
                                if (chatMessage.roomId != roomId) {
                                    logger.warn("Ignoring message for invalid room ${chatMessage.roomId}, expected $roomId")
                                    return@mono null
                                }

                                val normalizedMessage = chatMessage.copy(senderId = userId, roomId = roomId)
                                val savedMessage = saveMessageUseCase(normalizedMessage)
                                val userChatMessage = savedMessage.toUserChatMessage(userProfile)
                                val messageJson = objectMapper.writeValueAsString(userChatMessage)
                                chatBroadcaster.broadcastToRoom(roomId, messageJson)
                            }
                        }

                        meterRegistry.counter(
                            "takealook_chat_messages_total",
                            "scope",
                            "ws",
                            "type",
                            type.name.lowercase(),
                            "outcome",
                            "success"
                        ).increment()
                    } catch (ex: Exception) {
                        meterRegistry.counter(
                            "takealook_chat_messages_total",
                            "scope",
                            "ws",
                            "type",
                            "error",
                            "outcome",
                            "error"
                        ).increment()
                        logger.error("Incoming stream error for user $userId: ${ex.message}")
                        null
                    }
                }
            }
            .doOnError { e -> logger.error("Incoming stream error for user $userId: ${e.message}") }
            .then()

        incoming
            .doFinally { signalType ->
                MDC.remove("userId")
                MDC.remove("requestId")
                meterRegistry.counter("takealook_ws_connections_total", "outcome", "closed").increment()
                val isLastSession = chatBroadcaster.detachSession(userId, session)
                logger.info("Session closed for user $userId, Last: $isLastSession, Signal: $signalType")
                if (isLastSession) {
                    mono {
                        broadcastSystemMessage(roomId, userId, MessageType.LEAVE)
                    }.subscribe()
                }
            }
            .doOnError {
                meterRegistry.counter("takealook_ws_messages_error_total", "scope", "ws").increment()
            }
            .awaitSingleOrNull()
    }

    private suspend fun authenticateSession(headers: HttpHeaders, queryParams: Map<String, String>): Result<Long> {
        val ticket = queryParams["ticket"]?.trim()?.takeIf { it.isNotBlank() }
        if (ticket != null) {
            val ticketData = wsTicketService.validateAndConsumeTicket(ticket)
            return if (ticketData == null) {
                Result.failure(IllegalArgumentException("Invalid or expired ticket"))
            } else {
                Result.success(ticketData.userId)
            }
        }

        val token = extractToken(headers, queryParams)
            ?: return Result.failure(IllegalArgumentException("Missing auth token"))

        if (!jwtTokenProvider.isTokenValid(token)) {
            return Result.failure(IllegalArgumentException("Invalid JWT token"))
        }

        val principal = jwtTokenProvider.getAuthentication(token).principal
        val username = principal as? String ?: return Result.failure(IllegalArgumentException("Invalid token principal"))
        val user = getUserByNameUseCase(username) ?: return Result.failure(IllegalArgumentException("User not found"))
        val userId = user.id ?: return Result.failure(IllegalArgumentException("User id missing"))

        return Result.success(userId)
    }

    private fun extractToken(headers: HttpHeaders, queryParams: Map<String, String>): String? {
        val bearer = headers[AUTHORIZATION_HEADER]?.firstOrNull()?.trim()
        if (!bearer.isNullOrBlank()) {
            val normalized = if (bearer.startsWith("Bearer ", ignoreCase = true)) {
                bearer.substringAfter("Bearer ", "").trim()
            } else {
                bearer.trim()
            }

            return normalized.takeIf { it.isNotBlank() }
        }

        val legacy = headers[LEGACY_HEADER_STRING]?.firstOrNull()?.trim()
        if (!legacy.isNullOrBlank()) {
            return legacy
        }

        return queryParams["token"]?.trim()?.takeIf { it.isNotBlank() }
            ?: queryParams["accessToken"]?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun parseQueryParams(rawQuery: String?): Map<String, String> {
        val query = rawQuery ?: return emptyMap()
        if (query.isBlank()) return emptyMap()

        return query.split("&").associate {
            val parts = it.split("=", limit = 2)
            parts[0] to (parts.getOrNull(1) ?: "")
        }
    }

    private fun detectIncomingType(rawJson: String): MessageType {
        return try {
            val node = objectMapper.readTree(rawJson)
            val typeText = node.get("type")?.asText()?.uppercase()
            if (typeText == "REACTION") MessageType.REACTION else MessageType.CHAT
        } catch (ex: Exception) {
            MessageType.CHAT
        }
    }

    private suspend fun handleReactionCommand(command: ReactionCommand, roomId: Long, userId: Long) {
        if (command.roomId != roomId) {
            logger.warn("Ignoring reaction for room ${command.roomId}, expected $roomId")
            return
        }

        val action = command.action.lowercase()
        if (action != "add" && action != "remove") {
            logger.warn("Invalid reaction action: ${command.action}")
            return
        }

        if (action == "add") {
            addReactionUseCase(
                ChatReaction(
                    messageId = command.messageId,
                    userId = userId,
                    reaction = command.reaction,
                    createdAt = System.currentTimeMillis(),
                )
            )
        } else {
            removeReactionUseCase(command.messageId, userId, command.reaction)
        }

        val payload = UserChatReaction(
            roomId = command.roomId,
            messageId = command.messageId,
            userId = userId,
            reaction = command.reaction,
            createdAt = System.currentTimeMillis(),
        )

        val messageJson = objectMapper.writeValueAsString(payload)
        chatBroadcaster.broadcastToRoom(command.roomId, messageJson)
    }

    private suspend fun broadcastSystemMessage(roomId: Long, userId: Long, type: MessageType) {
        val profile = getUserProfileByIdUseCase(userId) ?: return
        val systemMessage = UserChatMessage(
            messageId = 0L,
            roomId = roomId,
            sender = profile,
            type = type,
            imageUrl = null,
        )
        val messageJson = objectMapper.writeValueAsString(systemMessage)
        chatBroadcaster.broadcastToRoom(roomId, messageJson)
    }
}
