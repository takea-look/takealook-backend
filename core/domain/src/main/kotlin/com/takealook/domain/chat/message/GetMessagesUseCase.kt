package com.takealook.domain.chat.message

import com.takealook.domain.exceptions.ProfileNotFoundException
import com.takealook.domain.user.profile.UserProfileRepository
import com.takealook.model.toUserChatMessage

class GetMessagesUseCase(
    private val chatMessagesRepository: ChatMessagesRepository,
    private val userProfileRepository: UserProfileRepository
) {
    suspend operator fun invoke(roomId: Long) = chatMessagesRepository
        .findByRoomId(roomId)
        .map { message ->
            val user = userProfileRepository.findByUserId(message.senderId)
            if (user == null) throw ProfileNotFoundException("user id(${message.senderId}) 를 찾을 수 없습니다.")


            message.toUserChatMessage(user)
        }
}
