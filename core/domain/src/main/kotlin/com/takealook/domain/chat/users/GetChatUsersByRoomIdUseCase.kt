package com.takealook.domain.chat.users

class GetChatUsersByRoomIdUseCase(
    val chatRoomUsersRepository: ChatRoomUsersRepository
) {
    suspend operator fun invoke(roomId: Long) = chatRoomUsersRepository.findByRoomId(roomId)
}