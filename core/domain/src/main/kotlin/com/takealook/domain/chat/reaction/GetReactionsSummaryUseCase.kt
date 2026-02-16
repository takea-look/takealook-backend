package com.takealook.domain.chat.reaction

class GetReactionsSummaryUseCase(
    private val repository: ChatReactionsRepository,
) {
    suspend operator fun invoke(messageId: Long): List<ReactionSummaryItem> =
        repository.getReactionsSummary(messageId)
}

data class ReactionSummaryItem(
    val reaction: String,
    val count: Long,
)
