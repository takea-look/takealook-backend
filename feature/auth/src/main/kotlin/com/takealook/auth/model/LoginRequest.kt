package com.takealook.auth.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "로그인/회원가입 요청")
data class LoginRequest(
    @Schema(description = "사용자 이름(ID)", example = "user1")
    val username: String,
    @Schema(description = "비밀번호", example = "password123")
    val password: String
)
