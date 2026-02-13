package com.takealook.push

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "푸시 발송 요청")
data class PushSendRequest(
    @Schema(description = "대상 사용자 ID", example = "1")
    val userId: Long,
    @Schema(description = "제목", example = "Takealook")
    val title: String,
    @Schema(description = "본문", example = "새 메시지가 도착했어요")
    val body: String,
    @Schema(description = "디바이스 토큰(예: FCM token)")
    val deviceToken: String? = null,
)

@Schema(description = "푸시 발송 응답")
data class PushSendResponse(
    val accepted: Boolean,
    val provider: String,
    val message: String,
)
