package com.takealook.auth

import com.takealook.auth.component.GoogleAuthService
import com.takealook.auth.component.JwtTokenProvider
import com.takealook.auth.exception.AuthFlowDeprecatedException
import com.takealook.auth.exception.UnsupportedSocialProviderException
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
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AuthControllerTest {

    private val getUserByNameUseCase = mockk<GetUserByNameUseCase>()
    private val saveUserUseCase = mockk<SaveUserUseCase>(relaxed = true)
    private val googleAuthService = mockk<GoogleAuthService>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
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
    fun `signin endpoint should be deprecated`() = runBlocking {
        val ex = assertThrows(AuthFlowDeprecatedException::class.java) {
            runBlocking {
                controller.deprecatedSignIn(mapOf("username" to "u", "password" to "p"))
            }
        }
        assertEquals("password login is deprecated. Use SNS login endpoint: /auth/google/signin, /auth/kakao/signin, /auth/apple/signin", ex.message)
    }

    @Test
    fun `google signin should return access and refresh token`() = runBlocking {
        val request = GoogleLoginRequest(idToken = "id-token")
        val user = User(id = 1L, username = "google_123", password = "pwd")

        coEvery { googleAuthService.verifyIdToken("id-token") } returns
            GoogleTokenInfo(sub = "123", email = null, email_verified = null, name = null, picture = null)
        coEvery { getUserByNameUseCase("google_123") } returns user
        coEvery { jwtTokenProvider.createToken("google_123") } returns "access-1"
        coEvery { jwtTokenProvider.createRefreshToken("google_123") } returns "refresh-1"

        val response = controller.loginWithGoogle(request)
        assertEquals("access-1", response.body?.accessToken)
        assertEquals("refresh-1", response.body?.refreshToken)
        coVerify(exactly = 1) { getUserByNameUseCase("google_123") }
    }

    @Test
    fun `google signin should create user when subject not found`() = runBlocking {
        val request = GoogleLoginRequest(idToken = "id-token")

        coEvery { googleAuthService.verifyIdToken("id-token") } returns
            GoogleTokenInfo(sub = "456", email = null, email_verified = null, name = null, picture = null)
        coEvery { getUserByNameUseCase("google_456") } returns null
        coEvery { jwtTokenProvider.createToken("google_456") } returns "access-2"
        coEvery { jwtTokenProvider.createRefreshToken("google_456") } returns "refresh-2"
        val response = controller.loginWithGoogle(request)
        assertEquals("access-2", response.body?.accessToken)
        assertEquals("refresh-2", response.body?.refreshToken)    }

    @Test
    fun `refresh token should be delegated to jwt provider`() = runBlocking {
        coEvery { jwtTokenProvider.refreshAccessToken("r") } returns "new-access"
        val response = controller.refresh(RefreshTokenRequest("r"))
        assertEquals("new-access", response.body?.accessToken)
    }

    @Test
    fun `unsupported providers should return standard exception`() = runBlocking {
        val ex = assertThrows(UnsupportedSocialProviderException::class.java) {
            runBlocking {
                controller.loginWithApple(mapOf("idToken" to "x"))
            }
        }
        assertEquals("Apple provider is planned. Current MVP supported provider: google. Use /auth/google/signin for sign-in.", ex.message)
    }
}
