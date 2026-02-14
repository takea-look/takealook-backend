package com.takealook.data.chat.message

import com.takealook.domain.chat.message.ChatMessagesRepository
import com.takealook.model.ChatMessage
import org.springframework.stereotype.Repository

@Repository
class ChatMessagesRepositoryImpl(
    private val repository: ChatMessagesR2dbcRepository
): ChatMessagesRepository {
    override suspend fun findByRoomId(roomId: Long, limit: Int, before: Long?): List<ChatMessage> {
        val safeLimit = limit.coerceIn(1, 100)

        val items = if (before != null) {
            repository.findRecentByRoomIdBefore(roomId, before, safeLimit)
        } else {
            repository.findRecentByRoomId(roomId, safeLimit)
        }

        return items.map { it.toExternal() }
    }

    override suspend fun findById(id: Long): ChatMessage? {
        return repository.findById(id)?.toExternal()
    }

    override suspend fun setBlinded(messageId: Long, blinded: Boolean): Boolean {
        return repository.setBlinded(messageId, blinded) > 0
    }

    override suspend fun saveMessage(message: ChatMessage) =
        repository.save(message.fromExternal()).toExternal()
}
