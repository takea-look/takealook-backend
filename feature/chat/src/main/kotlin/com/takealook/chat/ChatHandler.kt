package com.takealook.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.takealook.chat.reaction.ReactionCommand
import com.takealook.chat.ticket.WsTicketService
import com.takealook.domain.chat.message.SaveMessageUseCase
import com.takealook.domain.chat.reaction.AddReactionUseCase
import com.takealook.domain.chat.reaction.RemoveReactionUseCase
import com.takealook.domain.chat.users.GetChatUsersByRoomIdUseCase
import com.takealook.domain.user.profile.GetUserProfileByIdUseCase
import com.takealook.model.ChatMessage
import com.takealook.model.ChatReaction
import com.takealook.model.MessageType
import com.takealook.model.UserChatMessage
import com.takealook.model.UserChatReaction
import com.takealook.model.toUserChatMessage
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Controller
class ChatHandler(
    private val objectMapper: ObjectMapper,
    private val getChatUsersByRoomIdUseCase: GetChatUsersByRoomIdUseCase,
    private val getUserProfileByIdUseCase: GetUserProfileByIdUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val addReactionUseCase: AddReactionUseCase,
    private val removeReactionUseCase: RemoveReactionUseCase,
    private val wsTicketService: WsTicketService,
    private val chatBroadcaster: ChatBroadcaster,
    @Value("\${ws.allowed-origins:https://takealook.app,http://localhost:3000}")
    private val allowedOriginsConfig: String,
) : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(ChatHandler::class.java)

    private val allowedOrigins: Set<String> by lazy {
        allowedOriginsConfig.split(",").map { it.trim() }.toSet()
    }

    override fun handle(session: WebSocketSession): Mono<Void?> = mono {
        val origin = session.handshakeInfo.headers.origin
        if (origin != null && origin !in allowedOrigins) {
            logger.warn("Rejected connection from unauthorized origin: $origin")
            return@mono session.close(CloseStatus.POLICY_VIOLATION).awaitSingleOrNull()
        }

        val query = session.handshakeInfo.uri.query ?: ""
        val params = query.split("&").associate {
            val parts = it.split("=")
            parts[0] to (parts.getOrNull(1) ?: "")
        }

        val ticket = params["ticket"]
        val roomId = params["roomId"]?.toLongOrNull()

        if (ticket == null || roomId == null) {
            logger.warn("Missing ticket or roomId in WebSocket handshake")
            return@mono session.close(CloseStatus.POLICY_VIOLATION).awaitSingleOrNull()
        }

        val ticketData = wsTicketService.validateAndConsumeTicket(ticket)
        if (ticketData == null) {
            logger.warn("Invalid or expired ticket")
            return@mono session.close(CloseStatus.NOT_ACCEPTABLE).awaitSingleOrNull()
        }

        val userId = ticketData.userId

        val userProfile = getUserProfileByIdUseCase(userId) ?: run {
            logger.error("User not found: $userId")
            return@mono session.close(CloseStatus.BAD_DATA).awaitSingleOrNull()
        }

        val isRoomMember = getChatUsersByRoomIdUseCase(roomId).any { it.userId == userId }
        if (!isRoomMember) {
            logger.warn("User $userId is not a member of room $roomId")
            return@mono session.close(CloseStatus.NOT_ACCEPTABLE).awaitSingleOrNull()
        }

        val isFirstSession = chatBroadcaster.attachSession(userId, session)

        logger.info("WebSocket session established for user $userId, Room: $roomId, First: $isFirstSession")

        if (isFirstSession) {
            broadcastSystemMessage(roomId, userId, MessageType.JOIN)
        }

        val incoming = session.receive()
            .map { it.payloadAsText }
            .flatMap { rawMessage ->
                mono {
                    val type = detectIncomingType(rawMessage)
                    if (type == MessageType.REACTION) {
                        val command = objectMapper.readValue<ReactionCommand>(rawMessage)
                        handleReactionCommand(command, roomId, userId)
                        return@mono null
                    }

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
            .doOnError { e -> logger.error("Incoming stream error for user $userId: ${e.message}") }
            .then()

        Mono.when(incoming)
            .doFinally { signalType ->
                val isLastSession = chatBroadcaster.detachSession(userId, session)
                logger.info("Session closed for user $userId, Last: $isLastSession, Signal: $signalType")
                if (isLastSession) {
                    mono {
                        broadcastSystemMessage(roomId, userId, MessageType.LEAVE)
                    }.subscribe()
                }
            }
            .awaitSingleOrNull()
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
            roomId = roomId,
            sender = profile,
            type = type,
            imageUrl = null,
        )
        val messageJson = objectMapper.writeValueAsString(systemMessage)
        chatBroadcaster.broadcastToRoom(roomId, messageJson)
    }
}
