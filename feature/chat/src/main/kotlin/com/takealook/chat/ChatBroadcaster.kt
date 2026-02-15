package com.takealook.chat

import com.takealook.domain.chat.users.GetChatUsersByRoomIdUseCase
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

@Component
class ChatBroadcaster(
    private val getChatUsersByRoomIdUseCase: GetChatUsersByRoomIdUseCase,
) {
    private val logger = LoggerFactory.getLogger(ChatBroadcaster::class.java)
    private val sessions = ConcurrentHashMap<Long, MutableSet<WebSocketSession>>()

    fun attachSession(userId: Long, session: WebSocketSession): Boolean {
        val userSessions = sessions.computeIfAbsent(userId) { ConcurrentHashMap.newKeySet() }
        val isFirstSession = userSessions.isEmpty()
        userSessions.add(session)
        return isFirstSession
    }

    fun detachSession(userId: Long, session: WebSocketSession): Boolean {
        val userSessions = sessions[userId] ?: return false
        userSessions.remove(session)
        if (userSessions.isEmpty()) {
            sessions.remove(userId)
            return true
        }
        return false
    }

    suspend fun broadcastToRoom(roomId: Long, messageJson: String) {
        val users = getChatUsersByRoomIdUseCase(roomId)
        users.forEach { user ->
            val userSessions = sessions[user.userId] ?: return@forEach
            userSessions.toList().forEach { session ->
                if (!session.isOpen) {
                    userSessions.remove(session)
                    return@forEach
                }

                session.send(Mono.just(session.textMessage(messageJson)))
                    .doOnError { e ->
                        logger.error("Broadcast error to user ${user.userId}: ${e.message}")
                        userSessions.remove(session)
                    }
                    .onErrorResume { Mono.empty() }
                    .then()
                    .awaitSingleOrNull()
            }
        }
    }
}
