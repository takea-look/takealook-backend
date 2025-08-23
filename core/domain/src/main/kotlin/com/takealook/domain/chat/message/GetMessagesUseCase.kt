package com.takealook.domain.chat.message

import com.takealook.domain.user.UserRepository
import com.takealook.model.UserProfile
import com.takealook.model.toUserChatMessage

class GetMessagesUseCase(
    private val chatMessagesRepository: ChatMessagesRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(roomId: Long) = chatMessagesRepository
        .findByRoomId(roomId)
        .mapNotNull { message ->
            val user = userRepository.findByUserId(message.senderId)
            /**
             * TODO : 차후 UserProfile API 추가 필요
             * + UserProfileRepository
             */
            val profile = UserProfile(
                id = message.senderId,
                username = user?.username ?: return@mapNotNull null
            )

            message.toUserChatMessage(profile)
        }
}
