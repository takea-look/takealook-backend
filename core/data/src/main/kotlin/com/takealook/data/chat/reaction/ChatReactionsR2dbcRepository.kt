package com.takealook.data.chat.reaction

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ChatReactionsR2dbcRepository : CoroutineCrudRepository<ChatReactionsEntity, Long> {

    @Query("""
        DELETE FROM chat_message_reactions
        WHERE message_id = :messageId AND user_id = :userId AND reaction = :reaction
    """)
    suspend fun deleteByKey(messageId: Long, userId: Long, reaction: String): Long
}
