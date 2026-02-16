package com.takealook.observability

import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class MdcRequestFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val requestId = exchange.request.headers[REQUEST_ID_HEADER]?.firstOrNull()?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()

        val response = exchange.response
        response.headers.add(REQUEST_ID_HEADER, requestId)

        val request = exchange.request.mutate()
            .header(REQUEST_ID_HEADER, requestId)
            .build()

        val nextExchange = exchange.mutate().request(request).build()
        val userId = exchange.request.headers[USER_ID_HEADER]?.firstOrNull()?.trim()
            ?: "anonymous"

        return Mono.defer {
            MDC.put(REQUEST_ID_MDC_KEY, requestId)
            MDC.put(USER_ID_MDC_KEY, userId)
            chain.filter(nextExchange).doFinally {
                MDC.remove(REQUEST_ID_MDC_KEY)
                MDC.remove(USER_ID_MDC_KEY)
            }
        }
    }

    companion object {
        const val REQUEST_ID_HEADER = "X-Request-Id"
        const val USER_ID_HEADER = "X-User-Id"
        const val REQUEST_ID_MDC_KEY = "requestId"
        const val USER_ID_MDC_KEY = "userId"
    }
}
