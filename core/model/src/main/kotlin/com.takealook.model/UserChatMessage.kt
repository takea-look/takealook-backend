package com.takealook.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용자 채팅 메시지 정보")
data class UserChatMessage(
    @Schema(description = "채팅방 ID", example = "1")
    val roomId: Long,
    @Schema(description = "발신자 정보")
    val sender: UserProfile,
    @Schema(description = "메시지 타입", example = "CHAT")
    val type: MessageType = MessageType.CHAT,
    @Schema(description = "이미지 URL (스티커 등)", example = "http://example.com/sticker.png")
    val imageUrl: String? = null,
    @Schema(description = "답장 대상 메시지 ID", example = "100")
    val replyToId: Long? = null,
    @Schema(description = "생성일시 (타임스탬프)", example = "1672531200000")
    val createdAt: Long = System.currentTimeMillis()
)

fun ChatMessage.toUserChatMessage(
    profile: UserProfile
): UserChatMessage = UserChatMessage(
    roomId = roomId,
    sender = profile,
    type = MessageType.CHAT,
    imageUrl = imageUrl,
    replyToId = replyToId,
    createdAt = createdAt
)
