package com.takealook.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.takealook.auth.exception.GlobalExceptionHandler
import com.takealook.domain.user.GetUserByNameUseCase
import com.takealook.domain.user.SaveUserUseCase
import com.takealook.model.User
import com.takealook.model.auth.GoogleLoginRequest
import com.takealook.model.auth.GoogleTokenInfo
import com.takealook.model.auth.RefreshTokenRequest
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class AuthControllerIntegrationTest {

    private val getUserByNameUseCase = mockk<GetUserByNameUseCase>()
    private val saveUserUseCase = mockk<SaveUserUseCase>(relaxed = true)
    private val jwtTokenProvider = mockk<com.takealook.auth.component.JwtTokenProvider>()
    private val googleAuthService = mockk<com.takealook.auth.component.GoogleAuthService>()
    private val meterRegistry = mockk<MeterRegistry>(relaxed = true)

    private val controller = AuthController(
        getUserByNameUseCase,
        saveUserUseCase,
        jwtTokenProvider,
        googleAuthService,
        meterRegistry,
        120,
        60,
    )

    private val objectMapper = ObjectMapper().registerKotlinModule()

    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(controller)
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    @Test
    fun `oauth login success should return access token and refresh token`() {
        val request = GoogleLoginRequest(idToken = "valid-id-token")

        coEvery { googleAuthService.verifyIdToken("valid-id-token") } returns
            GoogleTokenInfo(sub = "google-sub", email = null, email_verified = null, name = null, picture = null)
        coEvery { getUserByNameUseCase("google_google-sub") } returns User(id = 1L, username = "google_google-sub", password = "pwd")
        coEvery { jwtTokenProvider.createToken("google_google-sub") } returns "access-token"
        coEvery { jwtTokenProvider.createRefreshToken("google_google-sub") } returns "refresh-token"

        val response = mockMvc.post("/auth/google/signin") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status().isOk
        }.andReturn()

        val body = response.response.contentAsString
        assertTrue(body.contains("\"accessToken\":\"access-token\""))
        assertTrue(body.contains("\"refreshToken\":\"refresh-token\""))
    }

    @Test
    fun `oauth login failure should return not implemented for unsupported provider`() {
        val response = mockMvc.post("/auth/apple/signin") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("idToken" to "id-token"))
        }.andExpect {
            status().isNotImplemented
        }.andReturn()

        assertTrue(response.response.contentAsString.contains("UNSUPPORTED_SOCIAL_PROVIDER"))
    }

    @Test
    fun `legacy signin should return deprecated status and message`() {
        val response = mockMvc.post("/auth/signin") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("username" to "u", "password" to "p"))
        }.andExpect {
            status().isGone
        }.andReturn()

        assertTrue(response.response.contentAsString.contains("AUTH_FLOW_DEPRECATED"))
    }

    @Test
    fun `refresh should return new access token`() {
        coEvery { jwtTokenProvider.refreshAccessToken("r") } returns "new-access"

        val response = mockMvc.post("/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshTokenRequest("r"))
        }.andExpect {
            status().isOk
        }.andReturn()

        assertTrue(response.response.contentAsString.contains("\"accessToken\":\"new-access\""))
        coVerify(exactly = 1) { jwtTokenProvider.refreshAccessToken("r") }
    }
}
