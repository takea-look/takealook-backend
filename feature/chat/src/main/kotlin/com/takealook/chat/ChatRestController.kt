package com.takealook.chat

import com.takealook.domain.chat.message.GetMessagesUseCase
import com.takealook.domain.chat.room.GetChatRoomsUseCase
import com.takealook.model.ChatRoom
import com.takealook.model.UserChatMessage
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Chat", description = "채팅 관리 API")
@RestController
@RequestMapping("/chat")
class ChatRestController(
    private val getChatRoomsUseCase: GetChatRoomsUseCase,
    private val getChatMessagesUseCase: GetMessagesUseCase,
) {

    @Operation(summary = "채팅방 목록 조회", description = "사용자가 참여 중인 채팅방 목록을 조회합니다.")
    @GetMapping("/rooms")
    suspend fun getRooms(): ResponseEntity<List<ChatRoom>> {
        val rooms = getChatRoomsUseCase()
        return ResponseEntity.ok(rooms.toList())
    }

    @Operation(summary = "채팅 메시지 조회", description = "특정 채팅방의 메시지 내역을 조회합니다.")
    @GetMapping("/messages")
    suspend fun getMessages(
        @RequestParam(required = true) roomId: Long
    ): ResponseEntity<List<UserChatMessage>> {
        val messages = getChatMessagesUseCase(roomId)
        return ResponseEntity.ok(messages)
    }
}
