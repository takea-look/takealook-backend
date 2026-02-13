package com.takealook.data.chat.report

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("chat_message_reports")
data class ChatMessageReportEntity(
    @Id val id: Long? = null,
    val messageId: Long,
    val reporterUserId: Long,
    val reason: String?,
    val createdAt: Long,
)
