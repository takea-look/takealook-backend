package com.takealook.data.chat.users

import com.takealook.domain.chat.users.ChatRoomUsersRepository
import com.takealook.model.ChatUser
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Repository

@Repository
class ChatRoomUsersRepositoryImpl(
    private val repository: ChatRoomUsersR2dbcRepository
): ChatRoomUsersRepository {
    override suspend fun findByRoomId(roomId: Long): List<ChatUser> =
        repository.findByRoomId(roomId)
            .map(ChatRoomUsersEntity::toExternal)
            .toList()

    override suspend fun existsByUserIdAndRoomId(userId: Long, roomId: Long): Boolean =
        repository.existsByUserIdAndRoomId(userId, roomId)

    override suspend fun save(chatUser: ChatUser): ChatUser =
        repository.save(chatUser.fromExternal()).toExternal()

    override suspend fun upsertIfNotExists(userId: Long, roomId: Long): ChatUser? =
        repository.upsertIfNotExists(userId, roomId, System.currentTimeMillis())?.toExternal()
}
