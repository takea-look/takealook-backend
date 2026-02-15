package com.takealook.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.takealook.domain.chat.message.GetMessagesUseCase
import com.takealook.domain.chat.message.SaveMessageUseCase
import com.takealook.domain.chat.room.CreateChatRoomUseCase
import com.takealook.domain.chat.room.GetChatRoomUseCase
import com.takealook.domain.chat.room.GetChatRoomsUseCase
import com.takealook.domain.chat.users.GetChatUsersByRoomIdUseCase
import com.takealook.domain.user.GetUserByNameUseCase
import com.takealook.domain.user.profile.GetUserProfileByIdUseCase
import com.takealook.model.ChatMessage
import com.takealook.model.ChatRoom
import com.takealook.model.UserChatMessage
import com.takealook.model.toUserChatMessage
import io.jsonwebtoken.Claims
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Tag(name = "Chat", description = "채팅 관리 API")
@RestController
@RequestMapping("/chat")
class ChatRestController(
    private val getChatRoomsUseCase: GetChatRoomsUseCase,
    private val getChatRoomUseCase: GetChatRoomUseCase,
    private val createChatRoomUseCase: CreateChatRoomUseCase,
    private val getChatMessagesUseCase: GetMessagesUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val getUserByNameUseCase: GetUserByNameUseCase,
    private val getUserProfileByIdUseCase: GetUserProfileByIdUseCase,
    private val getChatUsersByRoomIdUseCase: GetChatUsersByRoomIdUseCase,
    private val chatBroadcaster: ChatBroadcaster,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(ChatRestController::class.java)

    @Operation(summary = "채팅방 목록 조회", description = "사용자가 참여 중인 채팅방 목록을 조회합니다.")
    @GetMapping("/rooms")
    suspend fun getRooms(): ResponseEntity<List<ChatRoom>> {
        val rooms = getChatRoomsUseCase()
        return ResponseEntity.ok(rooms.toList())
    }

    data class CreateRoomRequest(
        val name: String,
        val isPublic: Boolean = true,
        val maxParticipants: Int = 0,
    )

    @Operation(summary = "채팅방 생성", description = "채팅방을 생성하고 id를 반환합니다.")
    @PostMapping("/rooms")
    suspend fun createRoom(
        @RequestBody body: CreateRoomRequest,
    ): ResponseEntity<ChatRoom> {
        val created = createChatRoomUseCase(body.name, body.isPublic, body.maxParticipants)
        return ResponseEntity.ok(created)
    }

    @Operation(summary = "채팅방 상세 조회", description = "채팅방 id로 상세를 조회합니다.")
    @GetMapping("/rooms/{id}")
    suspend fun getRoom(
        @PathVariable id: Long,
    ): ResponseEntity<ChatRoom> {
        val room = getChatRoomUseCase(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(room)
    }

    data class SendMessageRequest(
        val imageUrl: String,
        val replyToId: Long? = null,
    )

    @Operation(summary = "이미지 메시지 전송", description = "이미지 URL 기반으로 채팅 메시지를 보냅니다.")
    @PostMapping("/rooms/{roomId}/messages")
    suspend fun sendImageMessage(
        @AuthenticationPrincipal principal: Any?,
        @PathVariable roomId: Long,
        @RequestBody body: SendMessageRequest,
    ): ResponseEntity<UserChatMessage> {
        val username = extractUsername(principal)

        val user = getUserByNameUseCase(username)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")

        val senderId = user.id ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user")

        val roomUsers = getChatUsersByRoomIdUseCase(roomId)
        val isMember = roomUsers.any { it.userId == senderId }
        if (!isMember) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a room member")
        }

        val message = ChatMessage(
            roomId = roomId,
            senderId = senderId,
            imageUrl = body.imageUrl,
            replyToId = body.replyToId,
        )

        val profile = getUserProfileByIdUseCase(senderId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found")

        val saved = saveMessageUseCase(message)
        val userChatMessage = saved.toUserChatMessage(profile)
        val payload = objectMapper.writeValueAsString(userChatMessage)

        runCatching {
            chatBroadcaster.broadcastToRoom(roomId, payload)
        }.onFailure { error ->
            logger.warn("Broadcast failed after save message, room=$roomId user=$senderId", error)
        }

        return ResponseEntity.ok(userChatMessage)
    }

    @Operation(summary = "채팅 메시지 조회", description = "특정 채팅방의 메시지 내역을 조회합니다.")
    @GetMapping("/rooms/{roomId}/messages")
    suspend fun getMessagesByRoomId(
        @PathVariable roomId: Long,
        @RequestParam(required = false, defaultValue = "30") limit: Int,
        @RequestParam(required = false) before: Long?,
        @RequestParam(required = false) beforeMessageId: Long?,
    ): ResponseEntity<List<UserChatMessage>> {
        val messages = getChatMessagesUseCase(roomId, limit, before, beforeMessageId)
        return ResponseEntity.ok(messages)
    }

    @Deprecated("Compatibility: use /chat/rooms/{roomId}/messages")
    @Operation(summary = "채팅 메시지 조회", description = "특정 채팅방의 메시지 내역을 조회합니다. (호환용)")
    @GetMapping("/messages")
    suspend fun getMessages(
        @RequestParam(required = true) roomId: Long,
        @RequestParam(required = false, defaultValue = "30") limit: Int,
        @RequestParam(required = false) before: Long?,
        @RequestParam(required = false) beforeMessageId: Long?,
    ): ResponseEntity<List<UserChatMessage>> =
        getMessagesByRoomId(roomId, limit, before, beforeMessageId)

    private fun extractUsername(principal: Any?): String {
        return when (principal) {
            is Claims -> principal.subject
            is String -> principal
            else -> throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication")
        }
    }
}
