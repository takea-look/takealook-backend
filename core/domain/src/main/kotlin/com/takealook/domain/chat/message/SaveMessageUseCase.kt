package com.takealook.domain.chat.message

import com.takealook.model.ChatMessage

import com.takealook.domain.exceptions.InvalidReplyToMessageException

class SaveMessageUseCase(
    private val chatMessagesRepository: ChatMessagesRepository
) {
    suspend operator fun invoke(chatMessage: ChatMessage): ChatMessage {
        val replyToId = chatMessage.replyToId

        if (replyToId != null) {
            val replyTarget = chatMessagesRepository.findById(replyToId)
                ?: throw InvalidReplyToMessageException("replyToId(${replyToId}) message not found")

            if (replyTarget.roomId != chatMessage.roomId) {
                throw InvalidReplyToMessageException("replyToId(${replyToId}) must be in same room")
            }
        }

        return chatMessagesRepository.saveMessage(chatMessage)
    }
}
