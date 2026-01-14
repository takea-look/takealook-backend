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
import com.fasterxml.jackson.databind.ObjectMapper
import com.takealook.auth.component.TossAuthService
import com.takealook.auth.model.TossLoginRequest

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
        description = "토스 앱에서 받은 암호화된 토큰으로 로그인합니다.",
        security = []
    )
    @PostMapping("/toss/signin")
    suspend fun loginWithToss(@RequestBody request: TossLoginRequest): LoginResponse {
        val decryptedJson = tossAuthService.decryptToken(request.encryptedToken)
        
        val userInfo = objectMapper.readTree(decryptedJson)
        val tossUserId = userInfo.get("id")?.asText() 
            ?: throw InvalidCredentialsException("Invalid Toss Token Payload")
            
        val internalUsername = "toss_$tossUserId"
        
        var user = getUserByNameUseCase(internalUsername)
        if (user == null) {
            user = User(
                username = internalUsername,
                password = passwordEncoder.encode("TOSS_AUTH_USER")
            )
            saveUserUseCase(user)
        }
        
        val token = jwtTokenProvider.createToken(user.username)
        return LoginResponse(token)
    }
}

        val user = User(
            username = signupRequest.username,
            password = passwordEncoder.encode(signupRequest.password)
        )

        saveUserUseCase(user)
    }

    @Operation(
        summary = "토스 로그인",
        description = "토스 앱에서 받은 암호화된 토큰으로 로그인합니다.",
        security = []
    )
    @PostMapping("/toss/signin")
    suspend fun loginWithToss(@RequestBody request: TossLoginRequest): LoginResponse {
        // 1. Decrypt Token
        val decryptedJson = tossAuthService.decryptToken(request.encryptedToken)
        
        // 2. Parse User Info (Assuming 'id' is present in the payload)
        val userInfo = objectMapper.readTree(decryptedJson)
        val tossUserId = userInfo.get("id")?.asText() 
            ?: throw InvalidCredentialsException("Invalid Toss Token Payload")
            
        // 3. Map to Internal Username (e.g., "toss_{id}")
        val internalUsername = "toss_$tossUserId"
        
        // 4. Find or Create User
        var user = getUserByNameUseCase(internalUsername)
        if (user == null) {
            user = User(
                username = internalUsername,
                password = passwordEncoder.encode("TOSS_AUTH_USER") // Dummy password
            )
            saveUserUseCase(user)
        }
        
        // 5. Generate JWT
        val token = jwtTokenProvider.createToken(user.username)
        return LoginResponse(token)
    }
}