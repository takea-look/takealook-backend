package com.takealook.domain.chat.report

interface ChatMessageReportsRepository {
    suspend fun addReport(messageId: Long, reporterUserId: Long, reason: String?, createdAt: Long): Boolean
    suspend fun countReports(messageId: Long): Long
}
