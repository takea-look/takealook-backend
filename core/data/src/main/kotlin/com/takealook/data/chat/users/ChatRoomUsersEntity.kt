package com.takealook.data.chat.users

import com.takealook.model.ChatUser
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("chat_rooms")
data class ChatRoomUsersEntity(
    @Id val id: Long? = null,
    val userId: Long,
    val roomId: Long,
    val joinedAt: Long
)

fun ChatRoomUsersEntity.toExternal() = ChatUser(
    id = id,
    userId = userId,
    roomId = roomId,
    joinedAt = joinedAt
)

fun ChatUser.fromExternal() = ChatRoomUsersEntity(
    id = id,
    userId = userId,
    roomId = roomId,
    joinedAt = joinedAt
)
