package com.takealook.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "메시지 리액션")
data class ChatReaction(
    val id: Long? = null,
    val messageId: Long,
    val userId: Long,
    @Schema(description = "리액션 키(emoji 등)", example = "❤️")
    val reaction: String,
    val createdAt: Long = System.currentTimeMillis(),
)
