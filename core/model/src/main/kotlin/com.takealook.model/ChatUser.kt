package com.takealook.model

data class ChatUser(
    val id: Long? = null,
    val userId: Long,
    val roomId: Long,
    val joinedAt: Long
)
