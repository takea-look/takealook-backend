package com.takealook.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.takealook.chat.ticket.WsTicketService
import com.takealook.domain.chat.message.SaveMessageUseCase
import com.takealook.domain.chat.users.GetChatUsersByRoomIdUseCase
import com.takealook.domain.chat.users.JoinChatRoomUseCase
import com.takealook.domain.user.profile.GetUserProfileByIdUseCase
import com.takealook.model.ChatMessage
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
import java.util.concurrent.ConcurrentHashMap

@Controller
class ChatHandler(
    private val objectMapper: ObjectMapper,
    private val getChatUsersByRoomIdUseCase: GetChatUsersByRoomIdUseCase,
    private val getUserProfileByIdUseCase: GetUserProfileByIdUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val wsTicketService: WsTicketService,
    private val joinChatRoomUseCase: JoinChatRoomUseCase,
    @Value("\${ws.allowed-origins:https://takealook.app,http://localhost:3000}")
    private val allowedOriginsConfig: String
) : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(ChatHandler::class.java)
    // Multi-session support: one user can have multiple sessions (different browsers/devices)
    private val sessions = ConcurrentHashMap<Long, MutableSet<WebSocketSession>>()

    private val allowedOrigins: Set<String> by lazy {
        allowedOriginsConfig.split(",").map { it.trim() }.toSet()
    }

    override fun handle(session: WebSocketSession): Mono<Void?> = mono {
        val origin = session.handshakeInfo.headers.origin
        if (origin != null && origin !in allowedOrigins) {
            logger.warn("Rejected connection from unauthorized origin: $origin")
            return@mono session.close(CloseStatus.POLICY_VIOLATION).awaitSingleOrNull()
        }

        val ticket = session.handshakeInfo.uri.query
            ?.split("&")
            ?.firstOrNull { it.startsWith("ticket=") }
            ?.substringAfter("ticket=")

        if (ticket == null) {
            logger.warn("Missing ticket in WebSocket handshake for session ${session.id}")
            return@mono session.close(CloseStatus.POLICY_VIOLATION).awaitSingleOrNull()
        }

        val ticketData = wsTicketService.validateAndConsumeTicket(ticket)
        if (ticketData == null) {
            logger.warn("Invalid or expired ticket for session ${session.id}")
            return@mono session.close(CloseStatus.NOT_ACCEPTABLE).awaitSingleOrNull()
        }

        val userId = ticketData.userId

        val roomId = session.handshakeInfo.uri.query
            ?.split("&")
            ?.firstOrNull { it.startsWith("roomId=") }
            ?.substringAfter("roomId=")
            ?.toLongOrNull()

        if (roomId == null) {
            logger.warn("Missing roomId in WebSocket handshake for session ${session.id}")
            return@mono session.close(CloseStatus.POLICY_VIOLATION).awaitSingleOrNull()
        }

        getUserProfileByIdUseCase(userId) ?: run {
            logger.error("User not found in WebSocket session for ${session.id}")
            return@mono session.close(CloseStatus.BAD_DATA).awaitSingleOrNull()
        }

        joinChatRoomUseCase(userId, roomId)

        sessions.computeIfAbsent(userId) { ConcurrentHashMap.newKeySet() }.add(session)
        logger.info("WebSocket session established for user $userId (${ticketData.username}, Session ID: ${session.id}, Room: $roomId)")

        val incoming = session.receive()
            .map { it.payloadAsText }
            .flatMap { msg ->
                mono {
                    val chatMessage = objectMapper.readValue<ChatMessage>(msg)
                    val userProfile = getUserProfileByIdUseCase(userId) ?: return@mono null
                    val messageToSend = chatMessage
                        .toUserChatMessage(userProfile)
                        .let(objectMapper::writeValueAsString)

                    saveMessageUseCase(chatMessage)
                    logger.info("Received message from {}: {}", userId, chatMessage)

                    val users = getChatUsersByRoomIdUseCase(chatMessage.roomId)
                    users.forEach { user ->
                        val userSessions = sessions[user.userId] ?: return@forEach
                        userSessions.filter { it.isOpen }.forEach { targetSession ->
                            logger.info("Distributing message to user {} (session {}): {}", user.userId, targetSession.id, chatMessage)

                            targetSession.send(
                                Mono.just(
                                    targetSession.textMessage(messageToSend)
                                )
                            ).doOnError {
                                e -> logger.error("Error sending message to user ${user.userId}: ${e.message}", e)
                            }.onErrorResume { e ->
                                logger.error("Resume from send error to user ${user.userId}: ${e.message}", e)
                                Mono.empty()
                            }.awaitSingleOrNull()
                        }
                    }
                }
            }
            .doOnError { e -> logger.error("Error in incoming stream for user $userId: ${e.message}", e) }
            .then()

        return@mono Mono.`when`(incoming)
            .doFinally { signalType ->
                sessions.computeIfPresent(userId) { _, sessionSet ->
                    sessionSet.remove(session)
                    if (sessionSet.isEmpty()) null else sessionSet
                }
                logger.info("WebSocket session for user $userId (Session ID: ${session.id}) closed with status: $signalType")
            }
            .doOnError { e -> logger.error("WebSocket session for user $userId terminated unexpectedly: ${e.message}", e) }
            .awaitSingleOrNull()
    }
}
