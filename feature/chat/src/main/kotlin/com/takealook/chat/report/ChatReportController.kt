package com.takealook.chat.report

import com.takealook.domain.chat.report.ReportMessageUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Chat", description = "채팅 관리 API")
@RestController
@RequestMapping("/chat")
class ChatReportController(
    private val reportMessageUseCase: ReportMessageUseCase,
) {

    @Operation(summary = "메시지 신고", description = "고유 사용자 기준으로 신고를 누적하며, 10회 이상 시 자동 블라인드 처리합니다.")
    @PostMapping("/messages/report")
    suspend fun reportMessage(
        @RequestParam messageId: Long,
        @RequestParam reporterUserId: Long,
        @RequestParam(required = false) reason: String?,
    ) = reportMessageUseCase(messageId, reporterUserId, reason)
}
