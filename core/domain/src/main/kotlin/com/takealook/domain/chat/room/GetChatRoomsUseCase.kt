package com.takealook.domain.chat.room

import com.takealook.model.ChatRoom

class GetChatRoomsUseCase(
    private val repository: ChatRoomsRepository
) {
    suspend operator fun invoke(): List<ChatRoom> {
        return repository.getRooms()
    }
}
