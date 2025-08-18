package com.takealook.data.chat.users

import com.takealook.data.chat.room.ChatRoomsEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ChatRoomUsersR2dbcRepository : CoroutineCrudRepository<ChatRoomUsersEntity, Long> {
    fun findByRoomId(roomId: Long): Flow<ChatRoomUsersEntity>
}
