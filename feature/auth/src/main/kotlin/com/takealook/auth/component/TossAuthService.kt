package com.takealook.auth.component

import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.crypto.DirectDecrypter
import com.takealook.auth.client.TossApiClient
import com.takealook.auth.model.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64

@Service
class TossAuthService(
    @Value("\${toss.decryption-key:ZHVtbXkta2V5}") private val decryptionKey: String,
    @Value("\${toss.aad:TOSS}") private val aad: String,
    private val tossApiClient: TossApiClient
) {

    suspend fun exchangeToken(authorizationCode: String, referrer: String): Pair<String, String> {
        val response = tossApiClient.generateToken(
            GenerateTokenRequest(authorizationCode, referrer)
        )

        if (response.resultType != "SUCCESS" || response.success == null) {
            throw RuntimeException("Failed to generate token: ${response.resultType}")
        }

        return response.success.accessToken to response.success.refreshToken
    }

    suspend fun refreshAccessToken(refreshToken: String): String {
        val response = tossApiClient.refreshToken(
            RefreshTokenRequest(refreshToken)
        )

        if (response.resultType != "SUCCESS" || response.success == null) {
            throw RuntimeException("Failed to refresh token: ${response.resultType}")
        }

        return response.success.accessToken
    }

    suspend fun getUserInfo(accessToken: String): UserInfo {
        val response = tossApiClient.getUserInfo(accessToken)

        if (response.resultType != "SUCCESS" || response.success == null) {
            throw RuntimeException("Failed to get user info: ${response.resultType}")
        }

        return response.success
    }

    suspend fun logoutByAccessToken(accessToken: String) {
        val response = tossApiClient.logoutByAccessToken(accessToken)

        if (response.resultType != "SUCCESS") {
            throw RuntimeException("Failed to logout: ${response.resultType}")
        }
    }

    suspend fun logoutByUserKey(userKey: Long) {
        val response = tossApiClient.logoutByUserKey(userKey)

        if (response.resultType != "SUCCESS") {
            throw RuntimeException("Failed to logout by userKey: ${response.resultType}")
        }
    }

    fun decryptToken(encryptedToken: String): String {
        try {
            val jweObject = JWEObject.parse(encryptedToken)
            
            if (aad.isNotEmpty()) {
                val header = jweObject.header
            }
            
            val keyBytes = Base64.getDecoder().decode(decryptionKey)
            val decrypter = DirectDecrypter(keyBytes)
            
            jweObject.decrypt(decrypter)
            
            return jweObject.payload.toString()
        } catch (e: Exception) {
            throw RuntimeException("Failed to decrypt Toss token", e)
        }
    }
}
