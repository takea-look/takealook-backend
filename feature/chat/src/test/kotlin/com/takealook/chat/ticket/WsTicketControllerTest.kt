package com.takealook.chat.ticket

import com.takealook.domain.user.GetUserByNameUseCase
import com.takealook.model.User
import io.jsonwebtoken.Claims
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class WsTicketControllerTest {

    private lateinit var wsTicketService: WsTicketService
    private lateinit var getUserByNameUseCase: GetUserByNameUseCase
    private lateinit var controller: WsTicketController

    @BeforeEach
    fun setUp() {
        wsTicketService = mockk()
        getUserByNameUseCase = mockk()
        controller = WsTicketController(wsTicketService, getUserByNameUseCase)
    }

    @Test
    fun `getTicket should return ticket when authenticated with String principal`() = runTest {
        val testUser = User(id = 123L, username = "testuser", password = "hash")
        val expectedTicket = WsTicket(ticket = "uuid", expiresIn = 30)

        coEvery { getUserByNameUseCase("testuser") } returns testUser
        coEvery { wsTicketService.createTicket(123L, "testuser") } returns expectedTicket

        val authentication = UsernamePasswordAuthenticationToken("testuser", null, emptyList())
        val securityContext = SecurityContextImpl(authentication)

        val result = mono {
            controller.getTicket()
        }.contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))

        StepVerifier.create(result)
            .expectNextMatches { it.ticket == "uuid" && it.expiresIn == 30 }
            .verifyComplete()
    }

    @Test
    fun `getTicket should return ticket when authenticated with Claims principal`() = runTest {
        val testUser = User(id = 456L, username = "claimsuser", password = "hash")
        val expectedTicket = WsTicket(ticket = "claims-uuid", expiresIn = 30)

        val mockClaims = mockk<Claims>()
        every { mockClaims.subject } returns "claimsuser"

        coEvery { getUserByNameUseCase("claimsuser") } returns testUser
        coEvery { wsTicketService.createTicket(456L, "claimsuser") } returns expectedTicket

        val authentication = UsernamePasswordAuthenticationToken(mockClaims, null, emptyList())
        val securityContext = SecurityContextImpl(authentication)

        val result = mono {
            controller.getTicket()
        }.contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))

        StepVerifier.create(result)
            .expectNextMatches { it.ticket == "claims-uuid" && it.expiresIn == 30 }
            .verifyComplete()
    }

    @Test
    fun `getTicket should throw 401 when user not found`() = runTest {
        coEvery { getUserByNameUseCase("unknownuser") } returns null

        val authentication = UsernamePasswordAuthenticationToken("unknownuser", null, emptyList())
        val securityContext = SecurityContextImpl(authentication)

        val result = mono {
            controller.getTicket()
        }.contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))

        StepVerifier.create(result)
            .expectErrorMatches { error ->
                error is ResponseStatusException && error.statusCode == HttpStatus.UNAUTHORIZED
            }
            .verify()
    }

    @Test
    fun `getTicket should throw 401 when principal type is unexpected`() = runTest {
        val authentication = UsernamePasswordAuthenticationToken(12345, null, emptyList())
        val securityContext = SecurityContextImpl(authentication)

        val result = mono {
            controller.getTicket()
        }.contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))

        StepVerifier.create(result)
            .expectErrorMatches { error ->
                error is ResponseStatusException && error.statusCode == HttpStatus.UNAUTHORIZED
            }
            .verify()
    }
}
