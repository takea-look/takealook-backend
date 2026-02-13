package com.takealook.domain.chat.reaction

class RemoveReactionUseCase(
    private val repository: ChatReactionsRepository
) {
    suspend operator fun invoke(messageId: Long, userId: Long, reaction: String) =
        repository.remove(messageId, userId, reaction)
}
