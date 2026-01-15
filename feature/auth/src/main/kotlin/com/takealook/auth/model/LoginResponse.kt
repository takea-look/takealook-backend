package com.takealook.auth.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "로그인 응답")
data class LoginResponse(
    @Schema(description = "Access Token (JWT)", example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String,
    val refreshToken: String? = null
)
