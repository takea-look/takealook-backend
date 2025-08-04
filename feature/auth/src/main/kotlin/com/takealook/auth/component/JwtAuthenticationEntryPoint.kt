package com.takealook.auth.component

import com.fasterxml.jackson.databind.ObjectMapper
import com.takealook.auth.exception.GlobalExceptionHandler.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : ServerAuthenticationEntryPoint {
    override fun commence(
        exchange: ServerWebExchange?,
        ex: AuthenticationException?
    ): Mono<Void?>? {
        exchange ?: return null

        val response = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.contentType = MediaType.APPLICATION_JSON

        val dataBufferFactory = response.bufferFactory()
        val body = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            reason = "Unauthorized",
            message = "Unauthorized: ${ex?.message ?: "Invalid or missing token"}"
        )

        val jsonBody = objectMapper.writeValueAsString(body)
        val buffer = dataBufferFactory.wrap(jsonBody.toByteArray(StandardCharsets.UTF_8))

        return response.writeWith(Mono.just(buffer))
    }
}