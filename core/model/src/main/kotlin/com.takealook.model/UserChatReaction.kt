package com.takealook.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "리액션 이벤트(WS 브로드캐스트)")
data class UserChatReaction(
    @Schema(description = "채팅방 ID", example = "1")
    val roomId: Long,
    @Schema(description = "메시지 ID", example = "100")
    val messageId: Long,
    @Schema(description = "리액션을 누른 사용자 ID", example = "123")
    val userId: Long,
    @Schema(description = "리액션 키(emoji 등)", example = "❤️")
    val reaction: String,
    @Schema(description = "이벤트 타입")
    val type: MessageType = MessageType.REACTION,
    val createdAt: Long = System.currentTimeMillis(),
)
