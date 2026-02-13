package com.takealook.data.chat.report

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ChatMessageReportR2dbcRepository : CoroutineCrudRepository<ChatMessageReportEntity, Long> {

    @Query("""
        SELECT COUNT(*)
        FROM chat_message_reports
        WHERE message_id = :messageId
    """)
    suspend fun countByMessageId(messageId: Long): Long
}
