package com.takealook.auth

import com.takealook.domain.user.GetUserByNameUseCase
import com.takealook.domain.user.SaveUserUseCase
import com.takealook.auth.component.JwtTokenProvider
import com.takealook.auth.model.LoginRequest
import com.takealook.auth.model.LoginResponse
import com.takealook.domain.exceptions.InvalidCredentialsException
import com.takealook.domain.exceptions.UserAlreadyExistsException
import com.takealook.model.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Authentication", description = "인증 관리 API")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val getUserByNameUseCase: GetUserByNameUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
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
}