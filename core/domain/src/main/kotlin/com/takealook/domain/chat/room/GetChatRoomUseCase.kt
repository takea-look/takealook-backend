package com.takealook.domain.chat.room

class GetChatRoomUseCase(
    private val repository: ChatRoomsRepository
) {
    suspend operator fun invoke(id: Long) = repository.findById(id)
}
