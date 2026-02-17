package com.takealook.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class ChatControllerIntegrationTest {

    private val getChatRoomsUseCase = mockk<GetChatRoomsUseCase>()
    private val getChatRoomUseCase = mockk<GetChatRoomUseCase>()
    private val createChatRoomUseCase = mockk<CreateChatRoomUseCase>()
    private val getChatMessagesUseCase = mockk<GetMessagesUseCase>()
    private val saveMessageUseCase = mockk<SaveMessageUseCase>()
    private val getUserByNameUseCase = mockk<GetUserByNameUseCase>()
    private val getUserProfileByIdUseCase = mockk<GetUserProfileByIdUseCase>()
    private val getChatUsersByRoomIdUseCase = mockk<GetChatUsersByRoomIdUseCase>()
    private val chatBroadcaster = mockk<com.takealook.chat.ChatBroadcaster>(relaxed = true)
    private val objectMapper = ObjectMapper().registerKotlinModule()
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

    private val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(controller).build()

    @Test
    fun `chat send should persist and return user chat message`() {
        val roomId = 7L
        val request = ChatRestController.SendMessageRequest(imageUrl = "https://cdn/img.png", replyToId = 2L)
        coEvery { getUserByNameUseCase("claims-user") } returns User(id = 1L, username = "claims-user", password = "pw")
        coEvery { getChatUsersByRoomIdUseCase(roomId) } returns listOf(ChatUser(userId = 1L, roomId = roomId, joinedAt = 0L))
        coEvery { getUserProfileByIdUseCase(1L) } returns UserProfile(id = 1L, username = "claims-user", nickname = "nick")
        coEvery {
            saveMessageUseCase(any())
        } returns ChatMessage(
            id = 123L,
            roomId = roomId,
            senderId = 1L,
            imageUrl = "https://cdn/img.png",
            replyToId = 2L,
        )

        val response = mockMvc.post("/chat/rooms/$roomId/messages") {
            principal("claims-user")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andReturn().response

        assertEquals(200, response.status)
        assertTrue(response.contentAsString.contains("\"messageId\":123"))
        assertTrue(response.contentAsString.contains("\"roomId\":7"))
        
    }

    @Test
    fun `chat get messages should return payload list`() {
        val expected = listOf<UserChatMessage>()
        coEvery { getChatMessagesUseCase(1L, 20, 1000L, 10L) } returns expected

        val response = mockMvc.get("/chat/rooms/1/messages") {
            queryParam("limit", "20")
            queryParam("before", "1000")
            queryParam("beforeMessageId", "10")
        }.andReturn().response

        assertEquals(200, response.status)
        assertEquals("[]", response.contentAsString)
    }
}