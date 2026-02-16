package com.takealook.domain.limiter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AbuseRateLimiterTest {
    @Test
    fun `allow first requests inside window`() {
        val limiter = AbuseRateLimiter(maxRequestsPerWindow = 2, windowMs = 1000)

        assertTrue(limiter.canProceed("a", nowMs = 0L))
        assertTrue(limiter.canProceed("a", nowMs = 100L))
        assertFalse(limiter.canProceed("a", nowMs = 200L))
    }

    @Test
    fun `allow requests after window expires`() {
        val limiter = AbuseRateLimiter(maxRequestsPerWindow = 1, windowMs = 100)

        assertTrue(limiter.canProceed("a", nowMs = 0L))
        assertEquals(1L, limiter.retryAfterMillis("a", nowMs = 99L))
        assertFalse(limiter.canProceed("a", nowMs = 50L))
        assertTrue(limiter.canProceed("a", nowMs = 101L))
    }
}
