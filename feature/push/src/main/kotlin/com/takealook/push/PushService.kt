package com.takealook.push

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class PushService(
    @Value("\${push.provider:noop}")
    private val provider: String,
) {
    private val logger = LoggerFactory.getLogger(PushService::class.java)

    suspend fun send(request: PushSendRequest): PushSendResponse {
        // NOTE: provider integration(Firebase/APNS 등)은 추후 구현.
        // 지금은 서버/클라이언트 계약과 운영 wiring을 먼저 잡는다.
        logger.info("[PUSH] provider=$provider userId=${request.userId} title=${request.title}")

        return PushSendResponse(
            accepted = true,
            provider = provider,
            message = "accepted (noop)",
        )
    }
}
