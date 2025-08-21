package com.takealook.chat

import com.takealook.domain.chat.room.GetChatRoomsUseCase
import com.takealook.model.ChatRoom
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat/rooms")
class ChatRestController(
    private val getChatRoomsUseCase: GetChatRoomsUseCase,
) {

    @GetMapping
    suspend fun getRooms(): ResponseEntity<List<ChatRoom>> {
        val rooms = getChatRoomsUseCase()
        return ResponseEntity.ok(rooms.toList())
    }
}
