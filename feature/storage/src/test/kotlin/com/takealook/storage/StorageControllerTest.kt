package com.takealook.storage

import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class StorageControllerTest {

    private val service = mockk<StorageService>()
    private val meterRegistry = mockk<MeterRegistry>(relaxed = true)
    private val controller = StorageController(service, meterRegistry, 120, 60)

    @Test
    fun `getUploadUrl should return presign payload with canonical url`() {
        val payload = StorageService.UploadResponse(
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
                contentType = "image/png"
            )
        } returns payload

        val response = controller.getUploadUrl(
            key = "chat/1/1700000000000.png",
            sizeBytes = 1000L,
            contentType = "image/png"
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(payload, response.body)
    }
}
