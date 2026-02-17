package com.takealook.storage

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.verify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class StorageIntegrationTest {

    private val service = mockk<StorageService>()
    private val meterRegistry = mockk<MeterRegistry>(relaxed = true)

    private val uploadController = StorageController(service, meterRegistry, 120, 60)
    private val presignController = UploadPresignController(service, meterRegistry, 120, 60)

    private val objectMapper = ObjectMapper().registerKotlinModule()

    private val uploadMockMvc: MockMvc = MockMvcBuilders.standaloneSetup(uploadController).build()
    private val presignMockMvc: MockMvc = MockMvcBuilders.standaloneSetup(presignController).build()

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

        val response = uploadMockMvc.get("/storage/upload") {
            param("key", "chat/1/1700000000000.png")
            param("sizeBytes", "1000")
            param("contentType", "image/png")
        }.andReturn().response

        assertEquals(200, response.status)
        assertEquals(objectMapper.writeValueAsString(expected), response.contentAsString)
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

        val response = presignMockMvc.post("/uploads/presign") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andReturn().response

        assertEquals(200, response.status)
        assertEquals(objectMapper.writeValueAsString(expected), response.contentAsString)
        verify(exactly = 1) { service.generateChatMessageUploadKey(12L, "image/png") }
    }
}
