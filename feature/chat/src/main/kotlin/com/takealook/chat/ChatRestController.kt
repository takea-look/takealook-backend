package com.takealook.chat

import com.takealook.domain.chat.message.GetMessagesUseCase
import com.takealook.domain.chat.room.GetChatRoomsUseCase
import com.takealook.model.ChatMessage
import com.takealook.model.ChatRoom
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat")
class ChatRestController(
    private val getChatRoomsUseCase: GetChatRoomsUseCase,
    private val getChatMessagesUseCase: GetMessagesUseCase
) {

    @GetMapping("/rooms")
    suspend fun getRooms(): ResponseEntity<List<ChatRoom>> {
        val rooms = getChatRoomsUseCase()
        return ResponseEntity.ok(rooms.toList())
    }

    @GetMapping("/messages")
    suspend fun getMessages(
        @RequestParam(required = true) roomId: Long
    ): ResponseEntity<List<ChatMessage>> {
        val messages = getChatMessagesUseCase(roomId)
        return ResponseEntity.ok(messages)
    }
}
