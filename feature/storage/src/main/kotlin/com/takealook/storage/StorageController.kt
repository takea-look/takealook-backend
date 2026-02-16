package com.takealook.storage

import com.takealook.domain.limiter.AbuseRateLimiter
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Storage", description = "스토리지 관리 API")
@RestController
@RequestMapping("/storage")
class StorageController(
    private val service: StorageService,
    private val meterRegistry: MeterRegistry,
    @Value("\${abuse.upload.max-requests-per-minute:20}") private val maxUploadRequestPerMinute: Int,
    @Value("\${abuse.upload.window-seconds:60}") private val uploadWindowSeconds: Long,
) {
    private val logger = LoggerFactory.getLogger(StorageController::class.java)
    private val limiter = AbuseRateLimiter(maxUploadRequestPerMinute, uploadWindowSeconds * 1000)

    private fun resolveIdentity(
        userId: String?,
        forwardedFor: String?,
        realIp: String?,
        deviceId: String?,
    ): String {
        return if (!userId.isNullOrBlank()) {
            "user:${'$'}userId"
        } else {
            val ip = forwardedFor?.split(',')?.firstOrNull()?.trim() ?: (realIp?.trim() ?: "unknown-ip")
            val device = deviceId?.trim()?.takeIf { it.isNotBlank() } ?: "unknown-device"
            "ip:${'$'}ip:device:${'$'}device"
        }
    }

    @Operation(summary = "Presigned URL 생성", description = "키 기반으로 업로드용 Presigned URL을 생성합니다.")
    @GetMapping("/upload")
    fun getUploadUrl(
        @RequestParam key: String,
        @RequestParam(required = false) sizeBytes: Long?,
        @RequestParam(required = false) contentType: String?,
        @RequestHeader("X-User-Id", required = false) userId: String?,
        @RequestHeader(value = "X-Forwarded-For", required = false) forwardedFor: String?,
        @RequestHeader(value = "X-Real-IP", required = false) realIp: String?,
        @RequestHeader(value = "X-Device-Id", required = false) deviceId: String?,
    ): ResponseEntity<StorageService.UploadResponse> {
        val identity = resolveIdentity(userId, forwardedFor, realIp, deviceId)
        val limitKey = "storage-upload:${'$'}identity"
        if (!limiter.canProceed(limitKey)) {
            val retryAfterMs = limiter.retryAfterMillis(limitKey)
            val retryAfterSeconds = (retryAfterMs / 1000) + 1
            logger.warn("Rate limit exceeded for upload url. identity=${'$'}identity retryAfter=${'$'}retryAfterSeconds")
            meterRegistry.counter("takealook_abuse_rate_limited_total", "scope", "upload", "endpoint", "upload-url").increment()
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", retryAfterSeconds.toString())
                .build()
        }

        meterRegistry.counter("takealook_upload_url_requests_total", "outcome", "request").increment()
        return ResponseEntity.ok(service.uploadResponse(key = key, sizeBytes = sizeBytes, contentType = contentType))
    }
}
