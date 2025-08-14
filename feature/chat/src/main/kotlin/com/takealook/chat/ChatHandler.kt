package com.takealook.chat

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Controller
class ChatHandler : TextWebSocketHandler() {
    private val logger = LoggerFactory.getLogger(ChatHandler::class.java)

    /**
     * 만약 채팅 서버를 여러대 둔다고 하면, Redis에는 특정 세션id가 어떤 서버에 매핑되어있는지 까지 저장을 해둬야할 듯하다.
     */
    private val sessions = mutableSetOf<WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        super.afterConnectionEstablished(session)
        sessions += session

        logger.info("Connection successfully established for ${session.remoteAddress}")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        super.handleTextMessage(session, message)

        // TODO 1: room에 속해있는 userId 들을 조회하여야 함


        // TODO 2: userId를 기반으로 redis에서 특정 유저의 세션이 살아있는지 여부를 조회한다.
        // TODO 3: 세션이 살아있다면, message를 그대로 보내고, 살아있지 않다면 Push Notification 을 보내자 (+ Redis 필요)

        sessions.forEach { sess ->
            if (sess.isOpen) {
                /* TODO: message 의 형태는 ChatMessageEntity 를 기반으로 domain에 정의하여야 함 */
                val msg = message.payload
                sess.sendMessage(TextMessage(msg))
            } else {
                sessions -= sess
            }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        super.afterConnectionClosed(session, status)
        sessions -= session
        logger.info("Connection closed for ${session.remoteAddress}")
    }
}
