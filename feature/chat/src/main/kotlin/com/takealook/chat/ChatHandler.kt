package com.takealook.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.takealook.domain.chat.users.GetChatUsersByRoomIdUseCase
import com.takealook.domain.user.GetUserByIdUseCase
import com.takealook.model.ChatMessage
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
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
    private val getUserByIdUseCase: GetUserByIdUseCase,
) : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(ChatHandler::class.java)
    private val sessions = ConcurrentHashMap<Long, WebSocketSession>()

    override fun handle(session: WebSocketSession): Mono<Void?> = mono {
        val userId = session.handshakeInfo.uri.query.split("&")
            .firstOrNull { it.startsWith("userId=") }
            ?.substringAfter("userId=")
            ?.toLongOrNull()

        if (userId == null) {
            logger.error("User ID not found in WebSocket session for ${session.id}")
            return@mono session.close(CloseStatus.BAD_DATA).awaitSingleOrNull() // userId가 없으면 연결 종료
        }

        getUserByIdUseCase(userId) ?: run {
            logger.error("User not found in WebSocket session for ${session.id}")
            return@mono session.close(CloseStatus.BAD_DATA).awaitSingleOrNull()
        }

        sessions[userId] = session
        logger.info("New WebSocket session established for user $userId (Session ID: ${session.id})")

        // incoming message 처리
        val incoming = session.receive()
            .map { it.payloadAsText }
            .flatMap { msg ->
                mono {
                    val chatMessage = objectMapper.readValue<ChatMessage>(msg)
                    logger.info("Received message from {}: {}", userId, chatMessage)

                    val users = getChatUsersByRoomIdUseCase(chatMessage.roomId)
                    val messageToSend = objectMapper.writeValueAsString(chatMessage)
                    users.forEach { user ->
                        val targetSession = sessions[user.userId]
                        if (targetSession == null || !targetSession.isOpen) return@forEach
                        logger.info("Distributing message to user {}: {}", user.id, chatMessage)

                        targetSession.send(
                            Mono.just(
                                targetSession.textMessage(messageToSend)
                            )
                        ).doOnError {
                            e -> logger.error("Error sending message to user ${user.id}: ${e.message}", e)
                        }.awaitSingleOrNull()
                    }
                }
            }
            .doOnError { e -> logger.error("Error in incoming stream for user $userId: ${e.message}", e) }
            .then()

        return@mono Mono.`when`(incoming)
            .doFinally { signalType ->
                sessions.remove(userId)
                logger.info("WebSocket session for user $userId (Session ID: ${session.id}) closed with status: $signalType")
            }
            .doOnError { e -> logger.error("WebSocket session for user $userId terminated unexpectedly: ${e.message}", e) }
            .awaitSingleOrNull()
    }
}
