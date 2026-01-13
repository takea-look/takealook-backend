package com.takealook.data.chat.users

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.r2dbc.repository.Query

interface ChatRoomUsersR2dbcRepository : CoroutineCrudRepository<ChatRoomUsersEntity, Long> {
    fun findByRoomId(roomId: Long): Flow<ChatRoomUsersEntity>
    suspend fun existsByUserIdAndRoomId(userId: Long, roomId: Long): Boolean
    
    @Query("""
        INSERT INTO chat_room_users (user_id, room_id, joined_at)
        VALUES (:userId, :roomId, :joinedAt)
        ON CONFLICT (user_id, room_id) DO NOTHING
        RETURNING id, user_id, room_id, joined_at
    """)
    suspend fun upsertIfNotExists(userId: Long, roomId: Long, joinedAt: Long): ChatRoomUsersEntity?
}
