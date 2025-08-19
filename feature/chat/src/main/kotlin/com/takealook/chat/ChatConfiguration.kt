package com.takealook.chat

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@Configuration
class ChatConfiguration(
    private val chatHandler: ChatHandler,
) {

    @Bean
    fun webSocketMapping(): HandlerMapping {
        val map = mapOf(
            "/chat" to chatHandler
        )
        return  SimpleUrlHandlerMapping(map, 1)
    }

    @Bean
    fun handlerAdapter() = WebSocketHandlerAdapter()
}
