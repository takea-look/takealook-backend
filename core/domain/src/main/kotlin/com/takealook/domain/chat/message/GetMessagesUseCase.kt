package com.takealook.domain.chat.message

class GetMessagesUseCase(
    private val chatMessagesRepository: ChatMessagesRepository
) {
    suspend operator fun invoke(roomId: Long) =
        chatMessagesRepository.findByRoomId(roomId)
}
