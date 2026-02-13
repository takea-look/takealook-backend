package com.takealook.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "메시지 신고 요청")
data class ReportRequest(
    val messageId: Long,
    val reporterUserId: Long,
    val reason: String? = null,
)
