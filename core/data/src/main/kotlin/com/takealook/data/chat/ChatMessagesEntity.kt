package com.takealook.data.chat

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("chat_messages")
data class ChatMessagesEntity(
    @Id val id: Long? = null,
    val roomId: Long,
    val senderId: Long,
    val imageUrl: String,
    val replyToId: String,
    val createdAt: Long,
)
