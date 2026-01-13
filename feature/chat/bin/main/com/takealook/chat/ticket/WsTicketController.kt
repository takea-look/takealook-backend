package com.takealook.chat.ticket

import com.takealook.domain.user.GetUserByNameUseCase
import io.jsonwebtoken.Claims
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Tag(name = "Chat", description = "채팅 관리 API")
@RestController
@RequestMapping("/chat")
class WsTicketController(
    private val wsTicketService: WsTicketService,
    private val getUserByNameUseCase: GetUserByNameUseCase
) {

    @Operation(
        summary = "WebSocket 연결 티켓 발급",
        description = """
            WebSocket 채팅 연결에 사용할 일회용 티켓을 발급합니다.
            
            ## 사용 방법
            1. 이 API를 호출하여 티켓을 발급받습니다
            2. 30초 이내에 WebSocket 연결 시 티켓을 query parameter로 전달합니다
               - 예: ws://server/chat?ticket={ticket}
            3. 티켓은 일회용이며, 사용 후 즉시 폐기됩니다
            
            ## 주의사항
            - 티켓은 30초 후 만료됩니다
            - 동일 티켓은 재사용할 수 없습니다
            - WebSocket 연결 전에 반드시 이 API로 티켓을 발급받아야 합니다
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "티켓 발급 성공",
                content = [Content(schema = Schema(implementation = WsTicket::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 (accessToken 누락 또는 만료)"
            )
        ]
    )
    @PostMapping("/ticket")
    suspend fun getTicket(): WsTicket {
        val securityContext = ReactiveSecurityContextHolder.getContext().awaitSingle()
        val authentication = securityContext.authentication

        val username = when (val principal = authentication.principal) {
            is Claims -> principal.subject
            is String -> principal
            else -> throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication")
        }

        val user = getUserByNameUseCase(username)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")

        return wsTicketService.createTicket(user.id!!, user.username)
    }
}
