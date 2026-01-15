package com.takealook.auth.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.takealook.auth.model.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.stereotype.Component

@Component
class TossApiClient(
    private val okHttpClient: OkHttpClient,
    private val baseUrl: String,
    private val objectMapper: ObjectMapper
) {

    private val jsonMediaType = "application/json".toMediaType()

    private fun <T> get(
        path: String,
        responseType: Class<T>,
        headers: Map<String, String> = emptyMap()
    ): T {
        val requestBuilder = Request.Builder()
            .url("$baseUrl$path")
            .get()

        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        val request = requestBuilder.build()
        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw RuntimeException("Toss API error: ${response.code} - ${response.body?.string()}")
        }

        val responseBody = response.body?.string() ?: throw RuntimeException("Empty response")
        return objectMapper.readValue(responseBody, responseType)
    }

    private fun <T> post(
        path: String,
        requestBody: Any,
        responseType: Class<T>,
        headers: Map<String, String> = emptyMap()
    ): T {
        val requestBuilder = Request.Builder()
            .url("$baseUrl$path")
            .post(requestBody.toJson())

        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        val request = requestBuilder.build()
        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw RuntimeException("Toss API error: ${response.code} - ${response.body?.string()}")
        }

        val responseBody = response.body?.string() ?: throw RuntimeException("Empty response")
        return objectMapper.readValue(responseBody, responseType)
    }

    private fun Any.toJson(): RequestBody {
        val json = objectMapper.writeValueAsString(this)
        return json.toRequestBody(jsonMediaType)
    }

    fun generateToken(request: GenerateTokenRequest): GenerateTokenResponse {
        return post(
            "/api-partner/v1/apps-in-toss/user/oauth2/generate-token",
            request,
            GenerateTokenResponse::class.java
        )
    }

    fun refreshToken(request: RefreshTokenRequest): RefreshTokenResponse {
        return post(
            "/api-partner/v1/apps-in-toss/user/oauth2/refresh-token",
            request,
            RefreshTokenResponse::class.java
        )
    }

    fun getUserInfo(accessToken: String): GetUserInfoResponse {
        return get(
            "/api-partner/v1/apps-in-toss/user/oauth2/login-me",
            GetUserInfoResponse::class.java,
            mapOf("Authorization" to "Bearer $accessToken")
        )
    }

    fun logoutByAccessToken(accessToken: String): LogoutResponse {
        return post(
            "/api-partner/v1/apps-in-toss/user/oauth2/access/remove-by-access-token",
            EmptyRequest,
            LogoutResponse::class.java,
            mapOf("Authorization" to "Bearer $accessToken")
        )
    }

    fun logoutByUserKey(userKey: Long): LogoutResponse {
        return post(
            "/api-partner/v1/apps-in-toss/user/oauth2/access/remove-by-user-key",
            mapOf("userKey" to userKey),
            LogoutResponse::class.java
        )
    }

    private object EmptyRequest
}
