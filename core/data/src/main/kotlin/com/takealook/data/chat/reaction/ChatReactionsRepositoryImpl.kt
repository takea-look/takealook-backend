package com.takealook.data.chat.reaction

import com.takealook.domain.chat.reaction.ChatReactionsRepository
import com.takealook.domain.chat.reaction.ReactionSummaryItem
import com.takealook.model.ChatReaction
import org.springframework.stereotype.Repository

@Repository
class ChatReactionsRepositoryImpl(
    private val repository: ChatReactionsR2dbcRepository,
) : ChatReactionsRepository {

    override suspend fun add(reaction: ChatReaction): ChatReaction {
        return repository.save(reaction.fromExternal()).toExternal()
    }

    override suspend fun remove(messageId: Long, userId: Long, reaction: String): Boolean {
        return repository.deleteByKey(messageId, userId, reaction) > 0
    }

    override suspend fun getReactionsSummary(messageId: Long): List<ReactionSummaryItem> {
        return repository.countByMessageIdGrouped(messageId).map { row ->
            ReactionSummaryItem(reaction = row.reaction, count = row.cnt)
        }
    }
}
