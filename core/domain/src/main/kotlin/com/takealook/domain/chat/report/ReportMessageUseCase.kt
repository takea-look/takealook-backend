package com.takealook.domain.chat.report

import com.takealook.domain.chat.message.ChatMessagesRepository

class ReportMessageUseCase(
    private val reportsRepository: ChatMessageReportsRepository,
    private val messagesRepository: ChatMessagesRepository,
) {
    suspend operator fun invoke(messageId: Long, reporterUserId: Long, reason: String?, threshold: Long = 10): ReportResult {
        val createdAt = System.currentTimeMillis()

        val inserted = reportsRepository.addReport(messageId, reporterUserId, reason, createdAt)
        val count = reportsRepository.countReports(messageId)

        val blinded = if (count >= threshold) {
            messagesRepository.setBlinded(messageId, true)
            true
        } else {
            false
        }

        return ReportResult(
            messageId = messageId,
            inserted = inserted,
            reportCount = count,
            blinded = blinded,
        )
    }
}

data class ReportResult(
    val messageId: Long,
    val inserted: Boolean,
    val reportCount: Long,
    val blinded: Boolean,
)
