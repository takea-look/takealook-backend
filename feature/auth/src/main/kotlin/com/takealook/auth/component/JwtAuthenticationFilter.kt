package com.takealook.auth.component

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

const val LEGACY_HEADER_STRING = "accessToken"
const val AUTHORIZATION_HEADER = "Authorization"

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : WebFilter {

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void?> {

        val token = getAccessTokenFromRequestHeader(exchange.request)
        if (token?.isNotEmpty() == true &&  jwtTokenProvider.isTokenValid(token)) {
            val auth = jwtTokenProvider.getAuthentication(token)
            val context = SecurityContextImpl(auth)
            return chain.filter(exchange).contextWrite {
                ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context))
            }
        }
        return chain.filter(exchange)
    }

    private fun getAccessTokenFromRequestHeader(request: ServerHttpRequest): String? {
        // Standard: Authorization: Bearer <token>
        val authHeader = request.headers[AUTHORIZATION_HEADER]?.firstOrNull()
        val bearer = authHeader?.takeIf { StringUtils.hasText(it) }?.trim()

        if (bearer != null && bearer.startsWith("Bearer ", ignoreCase = true)) {
            return bearer.substringAfter("Bearer ").trim().takeIf { it.isNotBlank() }
        }

        // Legacy support: accessToken: <token>
        val legacy = request.headers[LEGACY_HEADER_STRING]?.firstOrNull()
        if (StringUtils.hasText(legacy)) {
            return legacy
        }

        return null
    }
}