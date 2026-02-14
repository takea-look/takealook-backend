package com.takealook.data.chat.message

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

import org.springframework.data.r2dbc.repository.Query

interface ChatMessagesR2dbcRepository : CoroutineCrudRepository<ChatMessagesEntity, Long> {

    @Query("""
        SELECT *
        FROM chat_messages
        WHERE room_id = :roomId
        ORDER BY created_at DESC, id DESC
        LIMIT :limit
    """)
    suspend fun findRecentByRoomId(roomId: Long, limit: Int): List<ChatMessagesEntity>

    @Query("""
        SELECT *
        FROM chat_messages
        WHERE room_id = :roomId
          AND created_at < :before
        ORDER BY created_at DESC, id DESC
        LIMIT :limit
    """)
    suspend fun findRecentByRoomIdBefore(roomId: Long, before: Long, limit: Int): List<ChatMessagesEntity>

    @Query("""
        SELECT *
        FROM chat_messages
        WHERE room_id = :roomId
          AND (created_at < :beforeCreatedAt OR (created_at = :beforeCreatedAt AND id < :beforeMessageId))
        ORDER BY created_at DESC, id DESC
        LIMIT :limit
    """)
    suspend fun findRecentByRoomIdBeforeMessage(
        roomId: Long,
        beforeCreatedAt: Long,
        beforeMessageId: Long,
        limit: Int,
    ): List<ChatMessagesEntity>

    @Query("""
        UPDATE chat_messages
        SET is_blinded = :blinded
        WHERE id = :messageId
    """)
    suspend fun setBlinded(messageId: Long, blinded: Boolean): Int
}