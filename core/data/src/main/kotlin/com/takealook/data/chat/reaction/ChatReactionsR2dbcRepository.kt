package com.takealook.data.chat.reaction

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ChatReactionsR2dbcRepository : CoroutineCrudRepository<ChatReactionsEntity, Long> {

    @Query("""
        DELETE FROM chat_message_reactions
        WHERE message_id = :messageId AND user_id = :userId AND reaction = :reaction
    """)
    suspend fun deleteByKey(messageId: Long, userId: Long, reaction: String): Long

    @Query("""
        SELECT reaction, COUNT(*) as cnt
        FROM chat_message_reactions
        WHERE message_id = :messageId
        GROUP BY reaction
        ORDER BY cnt DESC
    """)
    suspend fun countByMessageIdGrouped(messageId: Long): List<ReactionCountRow>
}

data class ReactionCountRow(
    val reaction: String,
    val cnt: Long,
)
