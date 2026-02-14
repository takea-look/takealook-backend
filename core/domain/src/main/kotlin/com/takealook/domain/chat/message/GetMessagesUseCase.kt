package com.takealook.domain.chat.message

import com.takealook.domain.exceptions.ProfileNotFoundException
import com.takealook.domain.user.profile.UserProfileRepository
import com.takealook.model.toUserChatMessage

import com.takealook.model.ReplyMessageSummary

class GetMessagesUseCase(
    private val chatMessagesRepository: ChatMessagesRepository,
    private val userProfileRepository: UserProfileRepository
) {
    suspend operator fun invoke(roomId: Long, limit: Int = 30, before: Long? = null) = chatMessagesRepository
        .findByRoomId(roomId, limit, before)
        .map { message ->
            val user = userProfileRepository.findByUserId(message.senderId)
            if (user == null) throw ProfileNotFoundException("user id(${message.senderId}) 를 찾을 수 없습니다.")

            val replyToSummary = message.replyToId
                ?.let { replyToId -> chatMessagesRepository.findById(replyToId) }
                ?.takeIf { it.roomId == roomId }
                ?.let { replyMessage ->
                    val replyUser = userProfileRepository.findByUserId(replyMessage.senderId)
                        ?: throw ProfileNotFoundException("user id(${replyMessage.senderId}) 를 찾을 수 없습니다.")

                    ReplyMessageSummary(
                        sender = replyUser,
                        imageUrl = replyMessage.imageUrl,
                        createdAt = replyMessage.createdAt,
                    )
                }

            message.toUserChatMessage(user, replyToSummary)
        }
}
