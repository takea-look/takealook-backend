package com.takealook.domain.chat.room

import com.takealook.model.ChatRoom

class CreateChatRoomUseCase(
    private val repository: ChatRoomsRepository
) {
    suspend operator fun invoke(name: String, isPublic: Boolean, maxParticipants: Int): ChatRoom {
        return repository.create(
            ChatRoom(
                name = name,
                isPublic = isPublic,
                maxParticipants = maxParticipants,
                createdAt = System.currentTimeMillis(),
            )
        )
    }
}
