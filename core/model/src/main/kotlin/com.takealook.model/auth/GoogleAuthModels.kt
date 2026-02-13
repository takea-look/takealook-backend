package com.takealook.model.auth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Google 로그인 요청")
data class GoogleLoginRequest(
    @Schema(description = "Google ID Token", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...")
    val idToken: String,
)

data class GoogleTokenInfo(
    val sub: String?,
    val email: String?,
    val email_verified: String?,
    val name: String?,
    val picture: String?,
)
