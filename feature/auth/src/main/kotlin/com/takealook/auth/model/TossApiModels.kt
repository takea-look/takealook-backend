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
    val success: UserInfo?
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

data class LogoutResponse(
    val resultType: String
)
