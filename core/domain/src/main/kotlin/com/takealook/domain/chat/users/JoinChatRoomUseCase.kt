package com.takealook.domain.chat.users

import com.takealook.model.ChatUser
import java.time.Clock

class JoinChatRoomUseCase(
    private val chatRoomUsersRepository: ChatRoomUsersRepository,
    private val clock: Clock = Clock.systemUTC()
) {
    suspend operator fun invoke(userId: Long, roomId: Long): ChatUser? =
        chatRoomUsersRepository.upsertIfNotExists(userId, roomId)
}
