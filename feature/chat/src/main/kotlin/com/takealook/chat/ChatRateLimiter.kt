package com.takealook.chat

import java.util.ArrayDeque
import java.util.concurrent.ConcurrentHashMap

class ChatRateLimiter(
    private val maxMessagesPerWindow: Int,
    private val windowMs: Long,
) {
    private val buckets = ConcurrentHashMap<Long, ArrayDeque<Long>>()

    init {
        require(maxMessagesPerWindow > 0) { "maxMessagesPerWindow must be greater than 0" }
        require(windowMs > 0) { "windowMs must be greater than 0" }
    }

    fun allow(userId: Long, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (maxMessagesPerWindow <= 0) return true
        val queue = buckets.computeIfAbsent(userId) { ArrayDeque() }
        synchronized(queue) {
            while (queue.isNotEmpty() && nowMs - queue.peekFirst() > windowMs) {
                queue.removeFirst()
            }
            if (queue.size >= maxMessagesPerWindow) {
                return false
            }
            queue.addLast(nowMs)
            return true
        }
    }
}
