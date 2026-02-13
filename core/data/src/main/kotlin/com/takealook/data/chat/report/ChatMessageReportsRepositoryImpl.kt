package com.takealook.data.chat.report

import com.takealook.domain.chat.report.ChatMessageReportsRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository

@Repository
class ChatMessageReportsRepositoryImpl(
    private val repository: ChatMessageReportR2dbcRepository,
) : ChatMessageReportsRepository {

    override suspend fun addReport(messageId: Long, reporterUserId: Long, reason: String?, createdAt: Long): Boolean {
        return try {
            repository.save(
                ChatMessageReportEntity(
                    messageId = messageId,
                    reporterUserId = reporterUserId,
                    reason = reason,
                    createdAt = createdAt,
                )
            )
            true
        } catch (_: DuplicateKeyException) {
            false
        }
    }

    override suspend fun countReports(messageId: Long): Long {
        return repository.countByMessageId(messageId)
    }
}
