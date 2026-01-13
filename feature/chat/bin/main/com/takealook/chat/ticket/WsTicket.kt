package com.takealook.chat.ticket

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "WebSocket 연결용 일회성 티켓")
data class WsTicket(
    @Schema(
        description = "WebSocket 연결 시 사용할 티켓 (일회용)",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    val ticket: String,

    @Schema(
        description = "티켓 유효 시간 (초)",
        example = "30"
    )
    val expiresIn: Int = 30
)

data class WsTicketData(
    val userId: Long,
    val username: String
)
