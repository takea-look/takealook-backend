package com.takealook.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.takealook.chat.ticket.WsTicketService
import com.takealook.domain.chat.message.SaveMessageUseCase
import com.takealook.domain.chat.users.GetChatUsersByRoomIdUseCase
import com.takealook.domain.chat.users.JoinChatRoomUseCase
import com.takealook.domain.user.profile.GetUserProfileByIdUseCase
import com.takealook.model.ChatMessage
import com.takealook.model.MessageType
import com.takealook.model.UserChatMessage
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

        joinChatRoomUseCase(userId, roomId)

        val userSessions = sessions.computeIfAbsent(userId) { ConcurrentHashMap.newKeySet() }
        val isFirstSession = userSessions.isEmpty()
        userSessions.add(session)
        
        logger.info("WebSocket session established for user $userId, Room: $roomId, First: $isFirstSession")

        // Broadcast JOIN message if it's the first session for this user
        if (isFirstSession) {
            broadcastSystemMessage(roomId, userId, MessageType.JOIN)
        }

        val incoming = session.receive()
            .map { it.payloadAsText }
            .flatMap { msg ->
                mono {
                    val chatMessage = objectMapper.readValue<ChatMessage>(msg)
                    val profile = getUserProfileByIdUseCase(userId) ?: return@mono null
                    val userChatMessage = chatMessage.toUserChatMessage(profile)
                    val messageToSend = objectMapper.writeValueAsString(userChatMessage)

                    saveMessageUseCase(chatMessage)
                    broadcastToRoom(chatMessage.roomId, messageToSend)
                }
            }
            .doOnError { e -> logger.error("Incoming stream error for user $userId: ${e.message}") }
            .then()

        return@mono Mono.`when`(incoming)
            .doFinally { signalType ->
                val remainingSessions = sessions.computeIfPresent(userId) { _, sessionSet ->
                    sessionSet.remove(session)
                    if (sessionSet.isEmpty()) null else sessionSet
                }
                
                val isLastSession = remainingSessions == null
                logger.info("Session closed for user $userId, Last: $isLastSession, Signal: $signalType")
                
                if (isLastSession) {
                    mono {
                        broadcastSystemMessage(roomId, userId, MessageType.LEAVE)
                    }.subscribe()
                }
            }
            .awaitSingleOrNull()
    }

    private suspend fun broadcastToRoom(roomId: Long, messageJson: String) {
        val users = getChatUsersByRoomIdUseCase(roomId)
        users.forEach { user ->
            sessions[user.userId]?.filter { it.isOpen }?.forEach { targetSession ->
                targetSession.send(Mono.just(targetSession.textMessage(messageJson)))
                    .doOnError { e -> logger.error("Broadcast error to user ${user.userId}: ${e.message}") }
                    .onErrorResume { Mono.empty() }
                    .awaitSingleOrNull()
            }
        }
    }

    private suspend fun broadcastSystemMessage(roomId: Long, userId: Long, type: MessageType) {
        val profile = getUserProfileByIdUseCase(userId) ?: return
        val systemMessage = UserChatMessage(
            roomId = roomId,
            sender = profile,
            type = type,
            imageUrl = null
        )
        val messageJson = objectMapper.writeValueAsString(systemMessage)
        broadcastToRoom(roomId, messageJson)
    }
}
