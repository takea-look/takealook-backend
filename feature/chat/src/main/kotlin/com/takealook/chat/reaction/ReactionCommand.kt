package com.takealook.chat.reaction

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "WS 리액션 커맨드")
data class ReactionCommand(
    val roomId: Long,
    val messageId: Long,
    val userId: Long,
    val reaction: String,
    val action: String = "add", // add|remove
)
