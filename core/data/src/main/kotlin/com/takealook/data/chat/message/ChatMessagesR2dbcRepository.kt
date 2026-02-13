package com.takealook.data.chat.message

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

import org.springframework.data.r2dbc.repository.Query

interface ChatMessagesR2dbcRepository : CoroutineCrudRepository<ChatMessagesEntity, Long> {
    suspend fun findByRoomId(roomId: Long): List<ChatMessagesEntity>

    @Query("""
        UPDATE chat_messages
        SET is_blinded = :blinded
        WHERE id = :messageId
    """)
    suspend fun setBlinded(messageId: Long, blinded: Boolean): Int
}