package com.takealook.auth.model

data class RefreshTokenRequest(
    val refreshToken: String
)

data class LogoutByUserKeyRequest(
    val userKey: Long
)
