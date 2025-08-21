package com.takealook.model

data class ChatRoom(
    val id: Long? = null,
    val name: String,
    val isPublic: Boolean,
    val maxParticipants: Int,
    val createdAt: Long,
)
