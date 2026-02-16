package com.takealook.auth

import com.takealook.auth.exception.AuthFlowDeprecatedException
import com.takealook.auth.exception.UnsupportedSocialProviderException
import com.takealook.domain.user.GetUserByNameUseCase
import com.takealook.domain.user.SaveUserUseCase
import com.takealook.auth.component.GoogleAuthService
import com.takealook.auth.component.JwtTokenProvider
import com.takealook.model.auth.GoogleLoginRequest
import com.takealook.model.auth.LoginResponse
import com.takealook.model.auth.RefreshTokenRequest
import com.takealook.domain.exceptions.InvalidCredentialsException
import com.takealook.model.User
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Authentication", description = "SNS 인증 관리 API")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val getUserByNameUseCase: GetUserByNameUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val jwtTokenProvider: JwtTokenProvider,
    private val googleAuthService: GoogleAuthService,
    private val meterRegistry: MeterRegistry,
) {

    private fun recordAuthEvent(provider: String, outcome: String) {
        meterRegistry.counter("takealook_auth_requests_total", "provider", provider, "outcome", outcome).increment()
    }

    @Operation(
        summary = "구버전 로그인(비권장)",
        description = "과거 호환용 username/password 로그인은 비활성화되었습니다.",
        deprecated = true,
    )
    @PostMapping("/signin")
    suspend fun deprecatedSignIn(@RequestBody body: Map<String, String>): Nothing {
        recordAuthEvent("password", "deprecated")
        throw AuthFlowDeprecatedException(
            "password login is deprecated. Use SNS login endpoint: /auth/google/signin, /auth/kakao/signin, /auth/apple/signin"
        )
    }

    @Operation(
        summary = "구버전 회원가입(비권장)",
        description = "과거 호환용 username/password 회원가입은 비활성화되었습니다.",
        deprecated = true,
    )
    @PostMapping("/signup")
    suspend fun deprecatedSignUp(@RequestBody body: Map<String, String>): Nothing {
        recordAuthEvent("password", "deprecated")
        throw AuthFlowDeprecatedException(
            "username/password signup is deprecated. Use SNS onboarding flow managed by provider."
        )
    }

    @Operation(
        summary = "Google 로그인",
        description = "Google ID Token으로 로그인합니다.",
        security = []
    )
    @PostMapping("/google/signin")
    suspend fun loginWithGoogle(@RequestBody request: GoogleLoginRequest): LoginResponse {
        return try {
            val tokenInfo = googleAuthService.verifyIdToken(request.idToken)
            val sub = tokenInfo.sub ?: throw RuntimeException("Invalid google token")

            var user = getUserByNameUseCase("google_$sub")
            if (user == null) {
                val randomPassword = java.util.UUID.randomUUID().toString()
                user = User(
                    username = "google_$sub",
                    password = randomPassword,
                )
                saveUserUseCase(user)
            }

            val accessToken = jwtTokenProvider.createToken(user.username)
            val refreshToken = jwtTokenProvider.createRefreshToken(user.username)
            recordAuthEvent("google", "success")
            LoginResponse(accessToken, refreshToken)
        } catch (ex: Exception) {
            recordAuthEvent("google", "error")
            throw ex
        }
    }

    @Operation(
        summary = "Apple 로그인",
        description = "Apple OAuth 로그인. (현재 MVP: 미지원 - 내부 활성화 요청 필요)",
        security = []
    )
    @PostMapping("/apple/signin")
    suspend fun loginWithApple(@RequestBody request: Map<String, String>): LoginResponse {
        recordAuthEvent("apple", "unsupported")
        throw UnsupportedSocialProviderException(
            "Apple provider is planned. Current MVP supported provider: google. Use /auth/google/signin for sign-in."
        )
    }

    @Operation(
        summary = "Kakao 로그인",
        description = "Kakao OAuth 로그인. (현재 MVP: 미지원 - 내부 활성화 요청 필요)",
        security = []
    )
    @PostMapping("/kakao/signin")
    suspend fun loginWithKakao(@RequestBody request: Map<String, String>): LoginResponse {
        recordAuthEvent("kakao", "unsupported")
        throw UnsupportedSocialProviderException(
            "Kakao provider is planned. Current MVP supported provider: google. Use /auth/google/signin for sign-in."
        )
    }

    @Operation(
        summary = "토큰 재발급",
        description = "Internal refresh token을 이용해 access token을 재발급합니다.",
        security = []
    )
    @PostMapping("/refresh")
    suspend fun refresh(@RequestBody request: RefreshTokenRequest): LoginResponse {
        return try {
            val newAccessToken = jwtTokenProvider.refreshAccessToken(request.refreshToken)
            recordAuthEvent("refresh", "success")
            LoginResponse(newAccessToken)
        } catch (ex: Exception) {
            recordAuthEvent("refresh", "error")
            throw ex
        }
    }

    @Operation(
        summary = "사용자 세션 확인",
        description = "임시 디버그용 API로 JWT를 파싱한 username 정보를 반환합니다."
    )
    @GetMapping("/me")
    suspend fun getSession(@RequestHeader("Authorization") token: String): ResponseEntity<Map<String, String>> {
        return try {
            val accessToken = token.removePrefix("Bearer ").trim()
            if (!jwtTokenProvider.isTokenValid(accessToken)) {
                recordAuthEvent("session", "error")
                throw InvalidCredentialsException("Invalid token")
            }
            val claims = jwtTokenProvider.parseClaims(accessToken)
            recordAuthEvent("session", "success")
            ResponseEntity.ok(mapOf("username" to claims.subject))
        } catch (ex: Exception) {
            if (ex !is InvalidCredentialsException) {
                recordAuthEvent("session", "error")
            }
            throw ex
        }
    }
}
