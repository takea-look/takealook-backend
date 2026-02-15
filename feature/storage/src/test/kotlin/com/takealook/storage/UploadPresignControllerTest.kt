package com.takealook.storage

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UploadPresignControllerTest {

    private val storageService = mockk<StorageService>()
    private val controller = UploadPresignController(storageService)

    @Test
    fun `presign image upload should build key and return presign payload`() {
        val request = PresignRequest(
            roomId = 12L,
            contentType = "image/png",
            sizeBytes = 1024L,
        )
        every { storageService.generateChatMessageUploadKey(12L, "image/png") } returns "chat/12/1700000000000.png"
        every {
            storageService.generateUploadUrl(
                key = "chat/12/1700000000000.png",
                sizeBytes = 1024L,
                contentType = "image/png"
            )
        } returns "https://upload.example.com/upload"

        val response = controller.presignImageUpload(request)

        assertEquals("https://upload.example.com/upload", response.body?.url)
        assertEquals("chat/12/1700000000000.png", response.body?.key)
        assertEquals(mapOf("Content-Type" to "image/png"), response.body?.headers)
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
