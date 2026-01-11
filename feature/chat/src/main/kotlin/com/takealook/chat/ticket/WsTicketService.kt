package com.takealook.chat.ticket

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

@Service
class WsTicketService(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${ws.ticket.ttl-seconds:30}")
    private val ttlSeconds: Long
) {
    private val logger = LoggerFactory.getLogger(WsTicketService::class.java)

    companion object {
        private const val TICKET_PREFIX = "ws-ticket:"
    }

    suspend fun createTicket(userId: Long, username: String): WsTicket {
        val ticket = UUID.randomUUID().toString()
        val data = WsTicketData(userId, username)
        val key = "$TICKET_PREFIX$ticket"

        redisTemplate.opsForValue()
            .set(key, objectMapper.writeValueAsString(data), Duration.ofSeconds(ttlSeconds))
            .awaitSingle()

        logger.info("Created WebSocket ticket for user $userId (TTL: ${ttlSeconds}s)")
        return WsTicket(ticket, ttlSeconds.toInt())
    }

    suspend fun validateAndConsumeTicket(ticket: String): WsTicketData? {
        val key = "$TICKET_PREFIX$ticket"

        val dataJson = redisTemplate.opsForValue()
            .getAndDelete(key)
            .awaitSingleOrNull()

        return dataJson?.let {
            try {
                val ticketData = objectMapper.readValue<WsTicketData>(it)
                logger.info("Validated and consumed ticket for user ${ticketData.userId}")
                ticketData
            } catch (e: Exception) {
                logger.error("Failed to parse ticket data: ${e.message}")
                null
            }
        }
    }
}
