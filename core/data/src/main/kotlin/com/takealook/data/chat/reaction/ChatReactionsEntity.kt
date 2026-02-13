package com.takealook.data.chat.reaction

import com.takealook.model.ChatReaction
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("chat_message_reactions")
data class ChatReactionsEntity(
    @Id val id: Long? = null,
    val messageId: Long,
    val userId: Long,
    val reaction: String,
    val createdAt: Long,
)

fun ChatReactionsEntity.toExternal() = ChatReaction(
    id = id,
    messageId = messageId,
    userId = userId,
    reaction = reaction,
    createdAt = createdAt,
)

fun ChatReaction.fromExternal() = ChatReactionsEntity(
    id = id,
    messageId = messageId,
    userId = userId,
    reaction = reaction,
    createdAt = createdAt,
)
