package com.takealook.data.chat.message

import com.takealook.domain.chat.message.ChatMessagesRepository
import com.takealook.model.ChatMessage
import org.springframework.stereotype.Repository

@Repository
class ChatMessagesRepositoryImpl(
    private val repository: ChatMessagesR2dbcRepository
): ChatMessagesRepository {
    override suspend fun findByRoomId(roomId: Long): List<ChatMessage> {
        return repository.findByRoomId(roomId).map { it.toExternal() }
    }

    override suspend fun saveMessage(message: ChatMessage) =
        repository.save(message.fromExternal()).toExternal()
}
