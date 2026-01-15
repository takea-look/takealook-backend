package com.takealook.auth.model

data class TossLoginRequest(
    val authorizationCode: String,
    val referrer: String
)
