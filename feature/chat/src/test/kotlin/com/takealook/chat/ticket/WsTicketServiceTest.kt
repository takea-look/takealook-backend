package com.takealook.chat.ticket

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import reactor.core.publisher.Mono
import java.time.Duration

class WsTicketServiceTest {

    private lateinit var redisTemplate: ReactiveRedisTemplate<String, String>
    private lateinit var valueOps: ReactiveValueOperations<String, String>
    private lateinit var objectMapper: ObjectMapper
    private lateinit var wsTicketService: WsTicketService

    @BeforeEach
    fun setUp() {
        redisTemplate = mockk()
        valueOps = mockk()
        objectMapper = jacksonObjectMapper()
        
        every { redisTemplate.opsForValue() } returns valueOps
        
        wsTicketService = WsTicketService(
            redisTemplate = redisTemplate,
            objectMapper = objectMapper,
            ttlSeconds = 30L
        )
    }

    @Test
    fun `createTicket should generate UUID and store in Redis with TTL`() = runTest {
        val keySlot = slot<String>()
        val valueSlot = slot<String>()
        val durationSlot = slot<Duration>()
        
        coEvery { 
            valueOps.set(capture(keySlot), capture(valueSlot), capture(durationSlot)) 
        } returns Mono.just(true)

        val result = wsTicketService.createTicket(userId = 123L, username = "testuser")

        assertNotNull(result.ticket)
        assertEquals(30, result.expiresIn)
        
        assert(keySlot.captured.startsWith("ws-ticket:"))
        
        val storedData = objectMapper.readValue(valueSlot.captured, WsTicketData::class.java)
        assertEquals(123L, storedData.userId)
        assertEquals("testuser", storedData.username)
        
        assertEquals(Duration.ofSeconds(30), durationSlot.captured)
    }

    @Test
    fun `validateAndConsumeTicket should return data and delete ticket when valid`() = runTest {
        val ticketId = "valid-ticket-uuid"
        val ticketData = WsTicketData(userId = 456L, username = "anotheruser")
        val storedJson = objectMapper.writeValueAsString(ticketData)
        
        coEvery { 
            valueOps.getAndDelete("ws-ticket:$ticketId") 
        } returns Mono.just(storedJson)

        val result = wsTicketService.validateAndConsumeTicket(ticketId)

        assertNotNull(result)
        assertEquals(456L, result?.userId)
        assertEquals("anotheruser", result?.username)
        
        coVerify { valueOps.getAndDelete("ws-ticket:$ticketId") }
    }

    @Test
    fun `validateAndConsumeTicket should return null when ticket not found`() = runTest {
        val invalidTicketId = "invalid-ticket"
        
        coEvery { 
            valueOps.getAndDelete("ws-ticket:$invalidTicketId") 
        } returns Mono.empty()

        val result = wsTicketService.validateAndConsumeTicket(invalidTicketId)

        assertNull(result)
    }

    @Test
    fun `validateAndConsumeTicket should return null when JSON parsing fails`() = runTest {
        val ticketId = "corrupted-ticket"
        
        coEvery { 
            valueOps.getAndDelete("ws-ticket:$ticketId") 
        } returns Mono.just("invalid-json-data")

        val result = wsTicketService.validateAndConsumeTicket(ticketId)

        assertNull(result)
    }

    @Test
    fun `ticket should be one-time use only`() = runTest {
        val ticketId = "one-time-ticket"
        val ticketData = WsTicketData(userId = 789L, username = "oneTimeUser")
        val storedJson = objectMapper.writeValueAsString(ticketData)
        
        coEvery { 
            valueOps.getAndDelete("ws-ticket:$ticketId") 
        } returnsMany listOf(Mono.just(storedJson), Mono.empty())

        val firstResult = wsTicketService.validateAndConsumeTicket(ticketId)
        assertNotNull(firstResult)
        assertEquals(789L, firstResult?.userId)

        val secondResult = wsTicketService.validateAndConsumeTicket(ticketId)
        assertNull(secondResult)
    }
}
