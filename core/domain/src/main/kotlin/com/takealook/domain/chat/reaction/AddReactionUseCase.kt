package com.takealook.domain.chat.reaction

import com.takealook.model.ChatReaction

class AddReactionUseCase(
    private val repository: ChatReactionsRepository
) {
    suspend operator fun invoke(reaction: ChatReaction) = repository.add(reaction)
}
