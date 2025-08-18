package com.takealook.domain.chat.users

import com.takealook.model.ChatUser

interface ChatRoomUsersRepository {
    suspend fun findByRoomId(roomId: Long): List<ChatUser>
}
