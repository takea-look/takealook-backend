package com.takealook.data.chat.room


import com.takealook.domain.chat.room.ChatRoomsRepository
import com.takealook.model.ChatRoom
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Repository

@Repository
class ChatRoomsRepositoryImpl(
    private val repository: ChatRoomsR2dbcRepository
): ChatRoomsRepository {
    override suspend fun getRooms(): List<ChatRoom> {
        return repository.findAll()
            .map { it.toExternal() }
            .toList()
    }
}
