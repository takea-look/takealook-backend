package com.takealook.auth.component

import com.fasterxml.jackson.databind.ObjectMapper
import com.takealook.model.auth.GoogleTokenInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import java.net.URLEncoder

@Service
class GoogleAuthService(
    private val okHttpClient: OkHttpClient,
    private val objectMapper: ObjectMapper,
) {

    suspend fun verifyIdToken(idToken: String): GoogleTokenInfo = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(idToken, Charsets.UTF_8)
        val req = Request.Builder()
            .url("https://oauth2.googleapis.com/tokeninfo?id_token=$encoded")
            .get()
            .build()

        val res = okHttpClient.newCall(req).execute()
        if (!res.isSuccessful) {
            throw RuntimeException("Google tokeninfo failed: ${res.code}")
        }

        val body = res.body?.string() ?: "{}"
        objectMapper.readValue(body, GoogleTokenInfo::class.java)
    }
}
