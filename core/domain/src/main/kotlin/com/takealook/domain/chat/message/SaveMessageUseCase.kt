package com.takealook.domain.chat.message

import com.takealook.model.ChatMessage

class SaveMessageUseCase(
    private val chatMessagesRepository: ChatMessagesRepository
) {
    suspend operator fun invoke(chatMessage: ChatMessage) =
        chatMessagesRepository.saveMessage(chatMessage)
}
