package com.takealook.auth

import com.takealook.domain.user.GetUserByNameUseCase
import com.takealook.domain.user.SaveUserUseCase
import com.takealook.auth.component.JwtTokenProvider
import com.takealook.auth.model.LoginRequest
import com.takealook.auth.model.LoginResponse
import com.takealook.auth.model.LogoutByUserKeyRequest
import com.takealook.auth.model.RefreshTokenRequest
import com.takealook.auth.model.TossLoginRequest
import com.takealook.auth.model.UserInfo
import com.takealook.domain.exceptions.InvalidCredentialsException
import com.takealook.domain.exceptions.UserAlreadyExistsException
import com.takealook.model.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.takealook.auth.component.TossAuthService
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID

@Tag(name = "Authentication", description = "인증 관리 API")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val getUserByNameUseCase: GetUserByNameUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val tossAuthService: TossAuthService,
    private val objectMapper: ObjectMapper
) {

    @Operation(
        summary = "로그인",
        description = "사용자 이름과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.",
        security = []
    )
    @PostMapping("/signin")
    suspend fun login(@RequestBody loginRequest: LoginRequest): LoginResponse {
        println("request : $loginRequest")
        val user = getUserByNameUseCase(loginRequest.username)
            ?: throw InvalidCredentialsException("Invalid username or password")

        if (!passwordEncoder.matches(loginRequest.password, user.password)) {
            throw InvalidCredentialsException("Invalid username or password")
        }

        val token = jwtTokenProvider.createToken(user.username)
        return LoginResponse(token)
    }

    @Operation(
        summary = "회원가입",
        description = "새로운 사용자를 등록합니다.",
        security = []
    )
    @PostMapping("/signup")
    suspend fun signUp(@RequestBody signupRequest: LoginRequest): Unit {
        if (getUserByNameUseCase(signupRequest.username) != null) {
            throw UserAlreadyExistsException("Username '${signupRequest.username}' is already taken.")
        }

        val user = User(
            username = signupRequest.username,
            password = passwordEncoder.encode(signupRequest.password)
        )

        saveUserUseCase(user)
    }

    @Operation(
        summary = "토스 로그인",
        description = "토스 앱에서 받은 인가 코드로 로그인합니다.",
        security = []
    )
    @PostMapping("/toss/signin")
    suspend fun loginWithToss(@RequestBody request: TossLoginRequest): LoginResponse {
        val (tossAccessToken, tossRefreshToken) = tossAuthService.exchangeToken(
            request.authorizationCode,
            request.referrer
        )

        val tossUserInfo = tossAuthService.getUserInfo(tossAccessToken)

        val internalUsername = "toss_${tossUserInfo.userKey}"

        var user = getUserByNameUseCase(internalUsername)
        if (user == null) {
            // Toss 사용자는 랜덤 비밀번호 사용 (패스워드 로그인 방지)
            val randomPassword = UUID.randomUUID().toString()
            
            user = User(
                username = internalUsername,
                password = passwordEncoder.encode(randomPassword),
                tossUserKey = tossUserInfo.userKey,
                tossName = tossUserInfo.name,
                tossPhone = tossUserInfo.phone,
                tossEmail = tossUserInfo.email
            )
            saveUserUseCase(user)
        } else {
            // 정보 업데이트
            user = user.copy(
                tossName = tossUserInfo.name,
                tossPhone = tossUserInfo.phone,
                tossEmail = tossUserInfo.email
            )
            saveUserUseCase(user)
        }

        val internalToken = jwtTokenProvider.createToken(user.username)
        return LoginResponse(internalToken, tossRefreshToken)
    }

    @Operation(
        summary = "토스 로그인 - 토큰 재발급",
        description = "Refresh token으로 access token을 재발급합니다.",
        security = []
    )
    @PostMapping("/toss/refresh")
    suspend fun refreshTossToken(
        @RequestBody request: RefreshTokenRequest
    ): LoginResponse {
        val newAccessToken = tossAuthService.refreshAccessToken(request.refreshToken)
        return LoginResponse(newAccessToken)
    }

    @Operation(
        summary = "토스 사용자 정보 조회",
        description = "Toss access token으로 사용자 정보를 조회합니다."
    )
    @GetMapping("/toss/userinfo")
    suspend fun getTossUserInfo(
        @RequestHeader("accessToken") accessToken: String
    ): UserInfo {
        return tossAuthService.getUserInfo(accessToken)
    }

    @Operation(
        summary = "토스 로그아웃 (Access Token)",
        description = "Access token으로 로그아웃합니다."
    )
    @PostMapping("/toss/logout")
    suspend fun logoutByAccessToken(
        @RequestHeader("accessToken") accessToken: String
    ) {
        tossAuthService.logoutByAccessToken(accessToken)
    }

    @Operation(
        summary = "토스 로그아웃 (User Key)",
        description = "User key로 로그아웃합니다.",
        security = []
    )
    @PostMapping("/toss/logout/user-key")
    suspend fun logoutByUserKey(
        @RequestBody request: LogoutByUserKeyRequest
    ) {
        tossAuthService.logoutByUserKey(request.userKey)
    }
}
