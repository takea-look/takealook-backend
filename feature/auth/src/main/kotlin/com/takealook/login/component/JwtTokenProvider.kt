package com.takealook.login.component

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    private val secretKey: String,

    @Value("\${jwt.expiration-time}")
    private val expirationTime: Long
) {

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    fun createToken(username: String): String {
        val now = Date()
        val validity = Date(now.time + expirationTime)

        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }
}
