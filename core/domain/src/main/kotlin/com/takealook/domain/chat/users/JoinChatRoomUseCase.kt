package com.takealook.domain.chat.users

import com.takealook.model.ChatUser

class JoinChatRoomUseCase(
    private val chatRoomUsersRepository: ChatRoomUsersRepository
) {
    suspend operator fun invoke(userId: Long, roomId: Long): ChatUser? {
        if (chatRoomUsersRepository.existsByUserIdAndRoomId(userId, roomId)) {
            return null
        }
        
        val chatUser = ChatUser(
            userId = userId,
            roomId = roomId,
            joinedAt = System.currentTimeMillis()
        )
        return chatRoomUsersRepository.save(chatUser)
    }
}
