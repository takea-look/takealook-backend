package com.takealook.login

import com.takealook.login.component.JwtTokenProvider
import com.takealook.login.exception.InvalidCredentialsException
import com.takealook.login.exception.UserAlreadyExistsException
import com.takealook.login.model.LoginRequest
import com.takealook.login.model.LoginResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userRepository: AuthRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @PostMapping("/signin")
    suspend fun login(@RequestBody loginRequest: LoginRequest): LoginResponse {
        val user = userRepository.findByUsername(loginRequest.username)
            ?: throw InvalidCredentialsException("Invalid username or password")

        if (!passwordEncoder.matches(loginRequest.password, user.password)) {
            throw InvalidCredentialsException("Invalid username or password")
        }

        val token = jwtTokenProvider.createToken(user.username)
        return LoginResponse(token)
    }

    @PostMapping("/signup")
    suspend fun signUp(@RequestBody signupRequest: LoginRequest): Unit {
        if (userRepository.findByUsername(signupRequest.username) != null) {
            throw UserAlreadyExistsException("Username '${signupRequest.username}' is already taken.")
        }

        val user = AuthInfo(
            username = signupRequest.username,
            password = passwordEncoder.encode(signupRequest.password)
        )
        userRepository.save(user)
    }
}