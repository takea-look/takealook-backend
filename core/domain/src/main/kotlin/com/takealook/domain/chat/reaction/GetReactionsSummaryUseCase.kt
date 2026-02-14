package com.takealook.domain.chat.reaction

import com.takealook.data.chat.reaction.ChatReactionsR2dbcRepository

class GetReactionsSummaryUseCase(
    private val repository: ChatReactionsR2dbcRepository,
) {
    suspend operator fun invoke(messageId: Long) =
        repository.countByMessageIdGrouped(messageId)
            .map { row -> ReactionSummaryItem(row.reaction, row.cnt) }
}

data class ReactionSummaryItem(
    val reaction: String,
    val count: Long,
)
