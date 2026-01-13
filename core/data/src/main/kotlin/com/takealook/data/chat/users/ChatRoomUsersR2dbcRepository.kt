package com.takealook.data.chat.users

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ChatRoomUsersR2dbcRepository : CoroutineCrudRepository<ChatRoomUsersEntity, Long> {
    fun findByRoomId(roomId: Long): Flow<ChatRoomUsersEntity>
    suspend fun existsByUserIdAndRoomId(userId: Long, roomId: Long): Boolean
}
