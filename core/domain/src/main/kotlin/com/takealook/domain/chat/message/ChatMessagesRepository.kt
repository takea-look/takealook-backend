package com.takealook.domain.chat.message

import com.takealook.model.ChatMessage

interface ChatMessagesRepository {
    suspend fun saveMessage(message: ChatMessage) : ChatMessage

    /**
     * @param before 기존 호환 파라미터(createdAt cursor)
     * @param beforeMessageId 개선된 커서(messageId cursor). 제공되면 before보다 우선합니다.
     */
    suspend fun findByRoomId(
        roomId: Long,
        limit: Int = 30,
        before: Long? = null,
        beforeMessageId: Long? = null,
    ): List<ChatMessage>

    suspend fun findById(id: Long): ChatMessage?
    suspend fun setBlinded(messageId: Long, blinded: Boolean): Boolean
}
