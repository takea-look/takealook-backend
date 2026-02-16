package com.takealook.chat

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ChatRateLimiterTest {

    @Test
    fun `rate limiter allows requests within window`() {
        val limiter = ChatRateLimiter(maxMessagesPerWindow = 2, windowMs = 1000)

        val now = 1_000L
        assertTrue(limiter.allow(1L, now))
        assertTrue(limiter.allow(1L, now + 500))
        assertFalse(limiter.allow(1L, now + 800))
    }

    @Test
    fun `rate limiter window is sliding and allows after expiration`() {
        val limiter = ChatRateLimiter(maxMessagesPerWindow = 1, windowMs = 1000)

        val now = 1_000L
        assertTrue(limiter.allow(10L, now))
        assertFalse(limiter.allow(10L, now + 500))
        assertTrue(limiter.allow(10L, now + 1501))
    }
}
