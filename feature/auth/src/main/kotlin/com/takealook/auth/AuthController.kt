package com.takealook.auth

import com.takealook.domain.GetUserByNameUseCase
import com.takealook.domain.SaveUserUseCase
import com.takealook.domain.User
import com.takealook.auth.component.JwtTokenProvider
import com.takealook.auth.exception.InvalidCredentialsException
import com.takealook.auth.exception.UserAlreadyExistsException
import com.takealook.auth.model.LoginRequest
import com.takealook.auth.model.LoginResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val getUserByNameUseCase: GetUserByNameUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

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