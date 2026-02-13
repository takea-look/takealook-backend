package com.takealook.model.auth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "로그인/회원가입 요청")
data class LoginRequest(
    @Schema(description = "사용자 이름(ID)", example = "user1")
    val username: String,
    @Schema(description = "비밀번호", example = "password123")
    val password: String
)

@Schema(description = "로그인 응답")
data class LoginResponse(
    @Schema(description = "Access Token (JWT)", example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String,
    @Schema(description = "Refresh Token (Toss OAuth)", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...")
    val refreshToken: String? = null
)

data class TossLoginRequest(
    val authorizationCode: String,
    val referrer: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class LogoutByUserKeyRequest(
    val userKey: Long
)

data class UserInfo(
    val userKey: Long,
    val scope: String?,
    val agreedTerms: List<String>?,
    val policy: String?,
    val certTxId: String?,
    val name: String?,
    val phone: String?,
    val birthday: String?,
    val gender: String?,
    val nationality: String?,
    val email: String?
)
