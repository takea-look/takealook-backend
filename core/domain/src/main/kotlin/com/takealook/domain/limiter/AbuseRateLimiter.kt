package com.takealook.domain.limiter

import java.util.ArrayDeque
import java.util.concurrent.ConcurrentHashMap

class AbuseRateLimiter(
    private val maxRequestsPerWindow: Int,
    private val windowMs: Long,
) {
    private val buckets = ConcurrentHashMap<String, ArrayDeque<Long>>()

    init {
        require(maxRequestsPerWindow > 0) { "maxRequestsPerWindow must be greater than 0" }
        require(windowMs > 0) { "windowMs must be greater than 0" }
    }

    fun canProceed(identity: String, nowMs: Long = System.currentTimeMillis()): Boolean {
        val bucket = buckets.computeIfAbsent(identity) { ArrayDeque() }

        synchronized(bucket) {
            purgeExpired(bucket, nowMs)
            if (bucket.size >= maxRequestsPerWindow) {
                return false
            }
            bucket.addLast(nowMs)
            return true
        }
    }

    fun retryAfterMillis(identity: String, nowMs: Long = System.currentTimeMillis()): Long {
        val bucket = buckets.computeIfAbsent(identity) { ArrayDeque() }

        synchronized(bucket) {
            purgeExpired(bucket, nowMs)
            if (bucket.size < maxRequestsPerWindow) {
                return 0L
            }
            val oldest = bucket.peekFirst()
            val remaining = windowMs - (nowMs - oldest)
            return remaining.coerceAtLeast(0L)
        }
    }

    private fun purgeExpired(bucket: ArrayDeque<Long>, nowMs: Long) {
        while (bucket.isNotEmpty() && nowMs - bucket.peekFirst() > windowMs) {
            bucket.removeFirst()
        }
    }
}
