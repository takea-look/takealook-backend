package com.takealook.storage

import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UploadPresignControllerTest {

    private val storageService = mockk<StorageService>()
    private val meterRegistry = mockk<MeterRegistry>(relaxed = true)
    private val controller = UploadPresignController(storageService, meterRegistry)

    @Test
    fun `presign image upload should build key and return presign payload`() {
        val request = PresignRequest(
            roomId = 12L,
            contentType = "image/png",
            sizeBytes = 1024L,
        )
        val expectedPayload = StorageService.UploadResponse(
            url = "https://upload.example.com/upload",
            key = "chat/12/1700000000000.png",
            canonicalUrl = "https://img.takealook.my/chat/12/1700000000000.png",
            headers = mapOf("Content-Type" to "image/png"),
            maxUploadBytes = 10L * 1024 * 1024,
            expiresInSeconds = 600,
        )
        every { storageService.generateChatMessageUploadKey(12L, "image/png") } returns "chat/12/1700000000000.png"
        every {
            storageService.uploadResponse(
                key = "chat/12/1700000000000.png",
                sizeBytes = 1024L,
                contentType = "image/png",
                headers = mapOf("Content-Type" to "image/png"),
            )
        } returns expectedPayload

        val response = controller.presignImageUpload(request)

        assertEquals(expectedPayload, response.body)
    }

    @Test
    fun `presign should throw when content type is invalid`() {
        val request = PresignRequest(roomId = 12L, contentType = "application/pdf")
        every { storageService.generateChatMessageUploadKey(12L, "application/pdf") } throws IllegalArgumentException("Unsupported mime type: application/pdf")

        try {
            controller.presignImageUpload(request)
            throw AssertionError("Expected IllegalArgumentException")
        } catch (ex: IllegalArgumentException) {
            assertEquals("Unsupported mime type: application/pdf", ex.message)
        }
    }
}
