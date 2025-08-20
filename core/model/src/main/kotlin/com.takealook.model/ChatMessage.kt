package com.takealook.model

data class ChatMessage(
    val id: Long? = null,
    val roomId: Long,
    val senderId: Long,
    val imageUrl: String,
    val replyToId: Long?,
    val createdAt: Long = System.currentTimeMillis(),
)
