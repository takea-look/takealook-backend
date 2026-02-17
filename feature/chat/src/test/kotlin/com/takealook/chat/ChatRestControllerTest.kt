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
import com.takealook.model.ChatUser
import com.takealook.model.User
import com.takealook.model.UserChatMessage
import com.takealook.model.UserProfile
import io.jsonwebtoken.Claims
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import io.micrometer.core.instrument.MeterRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ChatRestControllerTest {

    private val getChatRoomsUseCase = mockk<GetChatRoomsUseCase>()
    private val getChatRoomUseCase = mockk<GetChatRoomUseCase>()
    private val createChatRoomUseCase = mockk<CreateChatRoomUseCase>()
    private val getChatMessagesUseCase = mockk<GetMessagesUseCase>()
    private val saveMessageUseCase = mockk<SaveMessageUseCase>()
    private val getUserByNameUseCase = mockk<GetUserByNameUseCase>()
    private val getUserProfileByIdUseCase = mockk<GetUserProfileByIdUseCase>()
    private val getChatUsersByRoomIdUseCase = mockk<GetChatUsersByRoomIdUseCase>()
    private val chatBroadcaster = mockk<ChatBroadcaster>(relaxed = true)
    private val objectMapper = ObjectMapper().registerModule(com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
    private val meterRegistry = mockk<MeterRegistry>(relaxed = true)

    private val controller = ChatRestController(
        getChatRoomsUseCase,
        getChatRoomUseCase,
        createChatRoomUseCase,
        getChatMessagesUseCase,
        saveMessageUseCase,
        getUserByNameUseCase,
        getUserProfileByIdUseCase,
        getChatUsersByRoomIdUseCase,
        chatBroadcaster,
        objectMapper,
        meterRegistry,
        120,
        60,
    )

    @Test
    fun `send image message should save and return user-chat-message`() = runBlocking {
        val roomId = 7L
        val user = User(id = 1L, username = "u", password = "pw")
        val profile = UserProfile(id = 1L, username = "u", nickname = "nick")
        coEvery { getUserByNameUseCase("claims-user") } returns user
        coEvery { getChatUsersByRoomIdUseCase(roomId) } returns listOf(ChatUser(userId = 1L, roomId = roomId, joinedAt = 0L))
        coEvery { getUserProfileByIdUseCase(1L) } returns profile
        coEvery {
            saveMessageUseCase(any())
        } returns ChatMessage(
            id = 99L,
            roomId = roomId,
            senderId = 1L,
            imageUrl = "https://cdn/img.png",
            replyToId = 2L,
        )

        val claims = mockk<Claims>()
        every { claims.subject } returns "claims-user"

        val response = controller.sendImageMessage(claims, roomId, ChatRestController.SendMessageRequest(imageUrl = "https://cdn/img.png", replyToId = 2L), null, null, null)

        assertEquals(200, response.statusCode.value())
        assertEquals("https://cdn/img.png", response.body?.imageUrl)
        assertEquals(roomId, response.body?.roomId)
        assertEquals(1L, response.body?.sender?.id)

        coVerify(exactly = 1) {
            saveMessageUseCase(any())
        }
    }

    @Test
    fun `send image message should return 401 when principal is invalid`() = runBlocking {
        val ex = assertThrows<ResponseStatusException> {
            controller.sendImageMessage(123, 1L, ChatRestController.SendMessageRequest("https://cdn/img.png"), null, null, null)
        }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }

    @Test
    fun `send image message should return 403 when sender is not room member`() = runBlocking {
        coEvery { getUserByNameUseCase("u") } returns User(id = 1L, username = "u", password = "pw")
        coEvery { getChatUsersByRoomIdUseCase(10L) } returns emptyList()

        val ex = assertThrows<ResponseStatusException> {
            controller.sendImageMessage("u", 10L, ChatRestController.SendMessageRequest("https://cdn/img.png"), null, null, null)
        }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        assertEquals("User is not a room member", ex.reason)
    }

    @Test
    fun `get messages by room path should delegate to get messages use case`() = runBlocking {
        val expected = listOf<UserChatMessage>()
        coEvery { getChatMessagesUseCase(1L, 20, 1000L, 10L) } returns expected

        val response = controller.getMessagesByRoomId(roomId = 1L, limit = 20, before = 1000L, beforeMessageId = 10L)

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertEquals(expected, response.body)
    }

    @Test
    fun `legacy messages endpoint should delegate to room path endpoint`() = runBlocking {
        val expected = listOf<UserChatMessage>()
        coEvery { getChatMessagesUseCase(2L, 30, null, null) } returns expected

        val response = controller.getMessages(roomId = 2L, limit = 30, before = null, beforeMessageId = null)

        assertEquals(200, response.statusCode.value())
        assertEquals(expected, response.body)
    }
}
