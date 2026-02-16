package com.takealook.domain.chat.reaction

import com.takealook.model.ChatReaction

interface ChatReactionsRepository {
    suspend fun add(reaction: ChatReaction): ChatReaction
    suspend fun remove(messageId: Long, userId: Long, reaction: String): Boolean
    suspend fun getReactionsSummary(messageId: Long): List<ReactionSummaryItem>
}
