package com.takealook.domain.chat.room

import com.takealook.model.ChatRoom

interface ChatRoomsRepository {
    suspend fun getRooms(): List<ChatRoom>
}
