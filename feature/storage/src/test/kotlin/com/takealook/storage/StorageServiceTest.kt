package com.takealook.storage

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class StorageServiceTest {

    private val service = StorageService(
        props = StorageProps(
            accountId = "acc",
            accessKey = "ak",
            secretKey = "sk",
            bucket = "bucket",
            region = "auto",
            allowedExtensions = listOf("png", "jpg", "jpeg", "webp"),
        ),
        s3Presigner = mockk(relaxed = true)
    )

    @Test
    fun `chat message key should follow room timestamp extension format`() {
        val key = service.generateChatMessageUploadKey(10L, "image/png")

        assert(key.startsWith("chat/10/") && key.endsWith(".png"))
    }

    @Test
    fun `mimetype should resolve to extension`() {
        assertEquals("png", service.extensionForMime("image/png"))
        assertEquals("jpeg", service.extensionForMime("image/jpeg; charset=utf-8"))
        assertEquals("webp", service.extensionForMime("IMAGE/WEBP"))
    }

    @Test
    fun `invalid upload key should fail when format mismatch`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.validateChatUploadKeyAndSize("bad-key", 100L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            service.validateChatUploadKeyAndSize("chat/10/a.png", 100L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            service.validateChatUploadKeyAndSize("chat/10/1.gif", 100L)
        }
    }

    @Test
    fun `unsupported mimetype should fail`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.extensionForMime("text/plain")
        }
    }
}
