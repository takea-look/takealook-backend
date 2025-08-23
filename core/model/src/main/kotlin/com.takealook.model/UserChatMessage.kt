package com.takealook.model

data class UserChatMessage(
    val roomId: Long,
    val sender: UserProfile,
    val imageUrl: String,
    val replyToId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

fun ChatMessage.toUserChatMessage(
    profile: UserProfile
): UserChatMessage = UserChatMessage(
    roomId = roomId,
    sender = profile,
    imageUrl = imageUrl,
    replyToId = replyToId,
    createdAt = createdAt
)
