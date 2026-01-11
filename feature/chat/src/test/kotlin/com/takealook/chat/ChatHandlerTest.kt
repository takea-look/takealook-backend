package com.takealook.chat

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.takealook.chat.ticket.WsTicketData
import com.takealook.chat.ticket.WsTicketService
import com.takealook.domain.chat.message.SaveMessageUseCase
import com.takealook.domain.chat.users.GetChatUsersByRoomIdUseCase
import com.takealook.domain.user.profile.GetUserProfileByIdUseCase
import com.takealook.model.UserProfile
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.HandshakeInfo
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.net.URI
import java.time.LocalDateTime

class ChatHandlerTest {

    private lateinit var chatHandler: ChatHandler
    private lateinit var wsTicketService: WsTicketService
    private lateinit var getChatUsersByRoomIdUseCase: GetChatUsersByRoomIdUseCase
    private lateinit var getUserProfileByIdUseCase: GetUserProfileByIdUseCase
    private lateinit var saveMessageUseCase: SaveMessageUseCase

    @BeforeEach
    fun setUp() {
        wsTicketService = mockk()
        getChatUsersByRoomIdUseCase = mockk()
        getUserProfileByIdUseCase = mockk()
        saveMessageUseCase = mockk()

        chatHandler = ChatHandler(
            objectMapper = jacksonObjectMapper(),
            getChatUsersByRoomIdUseCase = getChatUsersByRoomIdUseCase,
            getUserProfileByIdUseCase = getUserProfileByIdUseCase,
            saveMessageUseCase = saveMessageUseCase,
            wsTicketService = wsTicketService,
            allowedOriginsConfig = "https://takealook.app,http://localhost:3000"
        )
    }

    private fun createMockSession(
        uri: URI,
        origin: String? = "https://takealook.app",
        sessionId: String = "test-session-id"
    ): WebSocketSession {
        val headers = mockk<HttpHeaders>()
        every { headers.origin } returns origin

        val handshakeInfo = mockk<HandshakeInfo>()
        every { handshakeInfo.uri } returns uri
        every { handshakeInfo.headers } returns headers

        val session = mockk<WebSocketSession>()
        every { session.handshakeInfo } returns handshakeInfo
        every { session.id } returns sessionId
        every { session.close(any()) } returns Mono.empty()

        return session
    }

    @Test
    fun `handle should close session when ticket is missing`() = runTest {
        val session = createMockSession(
            uri = URI.create("ws://localhost/chat")
        )

        val result = chatHandler.handle(session)

        StepVerifier.create(result)
            .verifyComplete()

        io.mockk.verify { session.close(CloseStatus.POLICY_VIOLATION) }
    }

    @Test
    fun `handle should close session when ticket is invalid or expired`() = runTest {
        val session = createMockSession(
            uri = URI.create("ws://localhost/chat?ticket=invalid-ticket")
        )

        coEvery { wsTicketService.validateAndConsumeTicket("invalid-ticket") } returns null

        val result = chatHandler.handle(session)

        StepVerifier.create(result)
            .verifyComplete()

        io.mockk.verify { session.close(CloseStatus.NOT_ACCEPTABLE) }
    }

    @Test
    fun `handle should close session when origin is not allowed`() = runTest {
        val session = createMockSession(
            uri = URI.create("ws://localhost/chat?ticket=valid-ticket"),
            origin = "https://evil-site.com"
        )

        val result = chatHandler.handle(session)

        StepVerifier.create(result)
            .verifyComplete()

        io.mockk.verify { session.close(CloseStatus.POLICY_VIOLATION) }
    }

    @Test
    fun `handle should close session when user not found after valid ticket`() = runTest {
        val session = createMockSession(
            uri = URI.create("ws://localhost/chat?ticket=valid-ticket")
        )

        val ticketData = WsTicketData(userId = 123L, username = "testuser")
        coEvery { wsTicketService.validateAndConsumeTicket("valid-ticket") } returns ticketData
        coEvery { getUserProfileByIdUseCase(123L) } returns null

        val result = chatHandler.handle(session)

        StepVerifier.create(result)
            .verifyComplete()

        io.mockk.verify { session.close(CloseStatus.BAD_DATA) }
    }

    @Test
    fun `handle should establish session when ticket is valid and user exists`() = runTest {
        val session = createMockSession(
            uri = URI.create("ws://localhost/chat?ticket=valid-ticket")
        )

        val ticketData = WsTicketData(userId = 456L, username = "validuser")
        val userProfile = UserProfile(
            id = 456L,
            username = "validuser",
            nickname = "Valid User",
            image = null,
            updatedAt = LocalDateTime.now()
        )

        coEvery { wsTicketService.validateAndConsumeTicket("valid-ticket") } returns ticketData
        coEvery { getUserProfileByIdUseCase(456L) } returns userProfile
        every { session.receive() } returns reactor.core.publisher.Flux.empty()

        val result = chatHandler.handle(session)

        StepVerifier.create(result)
            .verifyComplete()

        io.mockk.verify(exactly = 0) { session.close(any()) }
    }
}
