package com.takealook.chat

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.takealook.chat.ticket.WsTicketData
import com.takealook.chat.ticket.WsTicketService
import com.takealook.domain.chat.message.SaveMessageUseCase
import com.takealook.domain.chat.reaction.AddReactionUseCase
import com.takealook.domain.chat.reaction.RemoveReactionUseCase
import com.takealook.domain.chat.users.GetChatUsersByRoomIdUseCase
import com.takealook.domain.user.GetUserByNameUseCase
import com.takealook.domain.user.profile.GetUserProfileByIdUseCase
import com.takealook.model.ChatMessage
import com.takealook.model.UserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.HandshakeInfo
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.net.URI
import java.time.LocalDateTime

class ChatHandlerTest {

    private lateinit var chatHandler: ChatHandler
    private lateinit var wsTicketService: WsTicketService
    private lateinit var getChatUsersByRoomIdUseCase: GetChatUsersByRoomIdUseCase
    private lateinit var getUserByNameUseCase: GetUserByNameUseCase
    private lateinit var getUserProfileByIdUseCase: GetUserProfileByIdUseCase
    private lateinit var saveMessageUseCase: SaveMessageUseCase
    private lateinit var addReactionUseCase: AddReactionUseCase
    private lateinit var removeReactionUseCase: RemoveReactionUseCase
    private lateinit var chatBroadcaster: ChatBroadcaster
    private lateinit var jwtTokenProvider: com.takealook.auth.component.JwtTokenProvider

    @BeforeEach
    fun setUp() {
        wsTicketService = mockk()
        getChatUsersByRoomIdUseCase = mockk()
        getUserByNameUseCase = mockk()
        getUserProfileByIdUseCase = mockk()
        saveMessageUseCase = mockk()
        addReactionUseCase = mockk(relaxed = true)
        removeReactionUseCase = mockk(relaxed = true)
        chatBroadcaster = mockk(relaxed = true)
        jwtTokenProvider = mockk()

        chatHandler = ChatHandler(
            objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule()),
            getChatUsersByRoomIdUseCase = getChatUsersByRoomIdUseCase,
            getUserByNameUseCase = getUserByNameUseCase,
            getUserProfileByIdUseCase = getUserProfileByIdUseCase,
            saveMessageUseCase = saveMessageUseCase,
            addReactionUseCase = addReactionUseCase,
            removeReactionUseCase = removeReactionUseCase,
            wsTicketService = wsTicketService,
            jwtTokenProvider = jwtTokenProvider,
            chatBroadcaster = chatBroadcaster,
            allowedOriginsConfig = "https://takealook.app,http://localhost:3000"
        )

        coEvery { getChatUsersByRoomIdUseCase(1L) } returns listOf(com.takealook.model.ChatUser(userId = 10L, roomId = 1L, joinedAt = 0L))
    }

    private fun createMockSession(
        uri: URI,
        headers: HttpHeaders? = null,
        origin: String? = "https://takealook.app",
        sessionId: String = "test-session-id"
    ): WebSocketSession {
        val httpHeaders = headers ?: run {
            val h = mockk<HttpHeaders>(relaxed = true)
            every { h.origin } returns origin
            h
        }

        val handshakeInfo = mockk<HandshakeInfo>()
        every { handshakeInfo.uri } returns uri
        every { handshakeInfo.headers } returns httpHeaders

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

        verify { session.close(CloseStatus.POLICY_VIOLATION) }
    }

    @Test
    fun `handle should close session when ticket is invalid or expired`() = runTest {
        val session = createMockSession(
            uri = URI.create("ws://localhost/chat?ticket=invalid-ticket&roomId=1")
        )

        coEvery { wsTicketService.validateAndConsumeTicket("invalid-ticket") } returns null

        val result = chatHandler.handle(session)

        StepVerifier.create(result)
            .verifyComplete()

        verify { session.close(CloseStatus.NOT_ACCEPTABLE) }
    }

    @Test
    fun `handle should close session when origin is not allowed`() = runTest {
        val session = createMockSession(
            uri = URI.create("ws://localhost/chat?ticket=valid-ticket&roomId=1"),
            origin = "https://evil-site.com"
        )

        val result = chatHandler.handle(session)

        StepVerifier.create(result)
            .verifyComplete()

        verify { session.close(CloseStatus.POLICY_VIOLATION) }
    }

    @Test
    fun `handle should close session when ticket user is not room member`() = runTest {
        coEvery { wsTicketService.validateAndConsumeTicket("valid-ticket") } returns WsTicketData(userId = 123L, username = "u")
        coEvery { getUserProfileByIdUseCase(123L) } returns UserProfile(
            id = 123L,
            username = "u",
            nickname = "u",
            image = null,
            updatedAt = LocalDateTime.now(),
        )
        coEvery { getChatUsersByRoomIdUseCase(1L) } returns emptyList()

        val session = createMockSession(
            uri = URI.create("ws://localhost/chat?ticket=valid-ticket&roomId=1"),
            sessionId = "not-member-session"
        )

        val result = chatHandler.handle(session)

        StepVerifier.create(result)
            .verifyComplete()

        verify { session.close(CloseStatus.NOT_ACCEPTABLE) }
    }

    @Test
    fun `handle should establish session when ticket and membership valid`() = runTest {
        val session = createMockSession(
            uri = URI.create("ws://localhost/chat?ticket=valid-ticket&roomId=1")
        )

        val ticketData = WsTicketData(userId = 10L, username = "validuser")
        val userProfile = UserProfile(
            id = 10L,
            username = "validuser",
            nickname = "Valid User",
            image = null,
            updatedAt = LocalDateTime.now(),
        )

        coEvery { wsTicketService.validateAndConsumeTicket("valid-ticket") } returns ticketData
        coEvery { getUserProfileByIdUseCase(10L) } returns userProfile
        every { session.receive() } returns Flux.empty()
        every { chatBroadcaster.attachSession(10L, session) } returns true
        every { chatBroadcaster.detachSession(10L, session) } returns false

        val result = chatHandler.handle(session)

        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 0) { session.close(any()) }
    }

    @Test
    fun `ws should persist chat message with authenticated user id`() = runTest {
        val inbound = Flux.just(
            mockk<WebSocketMessage>(relaxed = true) {
                every { payloadAsText } returns jacksonObjectMapper().writeValueAsString(
                    ChatMessage(
                        id = null,
                        roomId = 1L,
                        senderId = 999L,
                        imageUrl = "https://cdn/img.png",
                        replyToId = null,
                    )
                )
            }
        )

        val session = createMockSession(uri = URI.create("ws://localhost/chat?ticket=valid-ticket&roomId=1"))
        coEvery { wsTicketService.validateAndConsumeTicket("valid-ticket") } returns WsTicketData(userId = 10L, username = "validuser")
        coEvery { getUserProfileByIdUseCase(10L) } returns UserProfile(
            id = 10L,
            username = "validuser",
            nickname = "Valid",
            image = null,
            updatedAt = LocalDateTime.now(),
        )
        coEvery { saveMessageUseCase(any()) } returns ChatMessage(
            id = 100L,
            roomId = 1L,
            senderId = 10L,
            imageUrl = "https://cdn/img.png",
            replyToId = null,
            createdAt = 1000L
        )
        every { session.receive() } returns inbound
        every { chatBroadcaster.detachSession(10L, session) } returns false

        val result = chatHandler.handle(session)

        StepVerifier.create(result)
            .thenCancel()
            .verify()
    }

    @Test
    fun `handle should authorize using token query when ticket missing`() = runTest {
        val token = "jwt-token-123"
        val session = createMockSession(
            uri = URI.create("ws://localhost/chat?roomId=1&token=$token")
        )

        every { jwtTokenProvider.isTokenValid(token) } returns true
        every {
            jwtTokenProvider.getAuthentication(token)
        } returns UsernamePasswordAuthenticationToken("token-user", token)

        coEvery { getUserByNameUseCase("token-user") } returns com.takealook.model.User(id = 10L, username = "token-user", password = "pw")
        coEvery { getUserProfileByIdUseCase(10L) } returns UserProfile(
            id = 10L,
            username = "token-user",
            nickname = "Token User",
            image = null,
            updatedAt = LocalDateTime.now(),
        )
        every { session.receive() } returns Flux.empty()
        every { chatBroadcaster.attachSession(10L, session) } returns false
        every { chatBroadcaster.detachSession(10L, session) } returns false

        val result = chatHandler.handle(session)

        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 0) { session.close(any()) }
    }

    @Test
    fun `handle should authorize using token query when ticket is blank`() = runTest {
        val token = "jwt-token-456"
        val session = createMockSession(
            uri = URI.create("ws://localhost/chat?roomId=1&ticket=&token=$token")
        )

        every { jwtTokenProvider.isTokenValid(token) } returns true
        every {
            jwtTokenProvider.getAuthentication(token)
        } returns UsernamePasswordAuthenticationToken("token-user-2", token)

        coEvery { getUserByNameUseCase("token-user-2") } returns com.takealook.model.User(id = 11L, username = "token-user-2", password = "pw")
        coEvery { getUserProfileByIdUseCase(11L) } returns UserProfile(
            id = 11L,
            username = "token-user-2",
            nickname = "Blank Ticket User",
            image = null,
            updatedAt = LocalDateTime.now(),
        )
        coEvery { getChatUsersByRoomIdUseCase(1L) } returns listOf(com.takealook.model.ChatUser(userId = 11L, roomId = 1L, joinedAt = 0L))
        every { session.receive() } returns Flux.empty()
        every { chatBroadcaster.attachSession(11L, session) } returns false
        every { chatBroadcaster.detachSession(11L, session) } returns false

        val result = chatHandler.handle(session)

        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 0) { session.close(any()) }
        coVerify(exactly = 0) { wsTicketService.validateAndConsumeTicket("") }
    }
}
