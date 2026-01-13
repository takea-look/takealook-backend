package com.takealook.domain.chat.users

import com.takealook.model.ChatUser

interface ChatRoomUsersRepository {
    suspend fun findByRoomId(roomId: Long): List<ChatUser>
    suspend fun existsByUserIdAndRoomId(userId: Long, roomId: Long): Boolean
    suspend fun save(chatUser: ChatUser): ChatUser
    suspend fun upsertIfNotExists(userId: Long, roomId: Long): ChatUser?
}
