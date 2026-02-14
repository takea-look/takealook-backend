package com.takealook.domain.chat.room

import com.takealook.model.ChatRoom

interface ChatRoomsRepository {
    suspend fun getRooms(): List<ChatRoom>
    suspend fun create(room: ChatRoom): ChatRoom
    suspend fun findById(id: Long): ChatRoom?
}
