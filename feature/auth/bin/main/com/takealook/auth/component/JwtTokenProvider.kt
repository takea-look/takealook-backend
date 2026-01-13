package com.takealook.auth.component

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
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
    private fun getParsedClaims(token: String) = Jwts
        .parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)

    fun getAuthentication(token: String): Authentication {
        val claims = getParsedClaims(token)

        val username = claims.payload
        return UsernamePasswordAuthenticationToken(
            username,
            token,
            emptyList()
        )
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            val claims = getParsedClaims(token)

            !claims.payload.expiration.before(Date())
        } catch(e: Exception) {
            false
        }
    }
}
