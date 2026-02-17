package com.takealook.storage

import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatusCode

class StorageIntegrationTest {

    private val service = mockk<StorageService>()
    private val meterRegistry = mockk<MeterRegistry>(relaxed = true)

    private val uploadController = StorageController(service, meterRegistry, 120, 60)
    private val presignController = UploadPresignController(service, meterRegistry, 120, 60)

    @Test
    fun `storage upload url endpoint should return presign payload`() {
        val expected = StorageService.UploadResponse(
            url = "https://upload.example.com/upload",
            key = "chat/1/1700000000000.png",
            canonicalUrl = "https://img.takealook.my/chat/1/1700000000000.png",
            headers = mapOf("Content-Type" to "image/png"),
            maxUploadBytes = 10L * 1024 * 1024,
            expiresInSeconds = 600,
        )

        every {
            service.uploadResponse(
                key = "chat/1/1700000000000.png",
                sizeBytes = 1000L,
                contentType = "image/png",
            )
        } returns expected

        val response = uploadController.getUploadUrl(
            key = "chat/1/1700000000000.png",
            sizeBytes = 1000L,
            contentType = "image/png",
            userId = null,
            forwardedFor = null,
            realIp = null,
            deviceId = null,
        )

        assertEquals(HttpStatusCode.valueOf(200), response.statusCode)
        assertEquals(expected, response.body)
    }

    @Test
    fun `upload presign endpoint should return generated payload`() {
        val request = PresignRequest(
            roomId = 12L,
            contentType = "image/png",
            sizeBytes = 1024L,
        )

        val expected = StorageService.UploadResponse(
            url = "https://upload.example.com/upload",
            key = "chat/12/1700000000000.png",
            canonicalUrl = "https://img.takealook.my/chat/12/1700000000000.png",
            headers = mapOf("Content-Type" to "image/png"),
            maxUploadBytes = 10L * 1024 * 1024,
            expiresInSeconds = 600,
        )

        every { service.generateChatMessageUploadKey(12L, "image/png") } returns "chat/12/1700000000000.png"
        every {
            service.uploadResponse(
                key = "chat/12/1700000000000.png",
                sizeBytes = 1024L,
                contentType = "image/png",
                headers = mapOf("Content-Type" to "image/png"),
            )
        } returns expected

        val response = presignController.presignImageUpload(
            body = request,
            userId = null,
            forwardedFor = null,
            realIp = null,
            deviceId = null,
        )

        assertEquals(HttpStatusCode.valueOf(200), response.statusCode)
        assertEquals(expected, response.body)
        verify(exactly = 1) { service.generateChatMessageUploadKey(12L, "image/png") }
    }
}
