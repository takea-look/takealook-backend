package com.takealook.data.chat.message

import com.takealook.data.chat.users.ChatRoomUsersEntity
import com.takealook.model.ChatMessage
import com.takealook.model.ChatUser
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("chat_messages")
data class ChatMessagesEntity(
    @Id val id: Long? = null,
    val roomId: Long,
    val senderId: Long,
    val imageUrl: String,
    val replyToId: Long?,
    val createdAt: Long,
)

fun ChatMessagesEntity.toExternal() = ChatMessage(
    id = id,
    roomId = roomId,
    senderId = senderId,
    imageUrl = imageUrl,
    replyToId = replyToId,
    createdAt = createdAt,
)

fun ChatMessage.fromExternal() = ChatMessagesEntity(
    id = id,
    roomId = roomId,
    senderId = senderId,
    imageUrl = imageUrl,
    replyToId = replyToId,
    createdAt = createdAt,
)
