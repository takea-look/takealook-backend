package com.takealook.domain.chat.message

import com.takealook.model.ChatMessage

interface ChatMessagesRepository {
    suspend fun saveMessage(message: ChatMessage) : ChatMessage
    suspend fun findByRoomId(roomId: Long): List<ChatMessage>
}
