package com.takealook.data.chat.message

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ChatMessagesR2dbcRepository : CoroutineCrudRepository<ChatMessagesEntity, Long> {
    suspend fun findByRoomId(roomId: Long): List<ChatMessagesEntity>
}