package com.takealook.domain.chat.message

import com.takealook.model.ChatMessage

interface ChatMessagesRepository {
    suspend fun saveMessage(message: ChatMessage) : ChatMessage
    suspend fun findByRoomId(roomId: Long, limit: Int = 30, before: Long? = null): List<ChatMessage>
    suspend fun findById(id: Long): ChatMessage?
    suspend fun setBlinded(messageId: Long, blinded: Boolean): Boolean
}
