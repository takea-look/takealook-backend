package com.takealook.auth.model

data class GenerateTokenRequest(
    val authorizationCode: String,
    val referrer: String
)

data class GenerateTokenResponse(
    val resultType: String,
    val success: SuccessData?
) {
    data class SuccessData(
        val tokenType: String,
        val accessToken: String,
        val refreshToken: String,
        val expiresIn: Long,
        val scope: String
    )
}

data class RefreshTokenResponse(
    val resultType: String,
    val success: GenerateTokenResponse.SuccessData?
)

data class GetUserInfoResponse(
    val resultType: String,
    val success: com.takealook.model.auth.UserInfo?
)

typealias UserInfo = com.takealook.model.auth.UserInfo

data class LogoutResponse(
    val resultType: String
)
