package com.takealook.data.chat.room

import com.takealook.model.ChatRoom
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("chat_rooms")
data class ChatRoomsEntity(
    @Id val id: Long? = null,
    val name: String,
    val isPublic: Boolean,
    val maxParticipants: Int,
    val createdAt: Long,
)

fun ChatRoomsEntity.toExternal() = ChatRoom(
    id = id,
    name = name,
    isPublic = isPublic,
    maxParticipants = maxParticipants,
    createdAt = createdAt,
)

fun ChatRoom.fromExternal() = ChatRoomsEntity(
    id = id,
    name = name,
    isPublic = isPublic,
    maxParticipants = maxParticipants,
    createdAt = createdAt,
)
