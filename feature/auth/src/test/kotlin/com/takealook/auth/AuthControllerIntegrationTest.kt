package com.takealook.auth

import com.takealook.auth.exception.AuthFlowDeprecatedException
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatusCode

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

    @Test
    fun `oauth login success should return access token and refresh token`() {
        val request = GoogleLoginRequest(idToken = "valid-id-token")

        coEvery { googleAuthService.verifyIdToken("valid-id-token") } returns
            GoogleTokenInfo(sub = "google-sub", email = null, email_verified = null, name = null, picture = null)
        coEvery { getUserByNameUseCase("google_google-sub") } returns User(id = 1L, username = "google_google-sub", password = "pwd")
        coEvery { jwtTokenProvider.createToken("google_google-sub") } returns "access-token"
        coEvery { jwtTokenProvider.createRefreshToken("google_google-sub") } returns "refresh-token"

        val response = controller.loginWithGoogle(request, null, null, null)

        assertEquals(HttpStatusCode.valueOf(200), response.statusCode)
        assertEquals("access-token", response.body?.accessToken)
        assertEquals("refresh-token", response.body?.refreshToken)
    }

    @Test
    fun `oauth login failure should throw unsupported provider exception`() {
        val response = runCatching {
            controller.loginWithApple(mapOf("idToken" to "id-token"), null, null, null)
        }

        assertEquals(
            true,
            response.exceptionOrNull() is com.takealook.auth.exception.UnsupportedSocialProviderException,
        )
    }

    @Test
    fun `legacy signin should throw deprecated flow exception`() {
        val response = runCatching {
            controller.deprecatedSignIn(mapOf("username" to "u", "password" to "p"), null, null, null)
        }

        assertEquals(true, response.exceptionOrNull() is AuthFlowDeprecatedException)
    }

    @Test
    fun `refresh should return new access token`() {
        coEvery { jwtTokenProvider.refreshAccessToken("r") } returns "new-access"

        val response = controller.refresh(RefreshTokenRequest("r"), null, null, null)

        assertEquals(HttpStatusCode.valueOf(200), response.statusCode)
        assertEquals("new-access", response.body?.accessToken)

        coVerify(exactly = 1) { jwtTokenProvider.refreshAccessToken("r") }
    }
}
