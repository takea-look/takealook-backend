package com.takealook.chat.reaction

import com.takealook.domain.chat.reaction.GetReactionsSummaryUseCase
import com.takealook.domain.chat.reaction.ReactionSummaryItem
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Chat", description = "채팅 관리 API")
@RestController
@RequestMapping("/chat/messages")
class ReactionRestController(
    private val getReactionsSummaryUseCase: GetReactionsSummaryUseCase,
) {

    @Operation(summary = "메시지 리액션 요약 조회", description = "메시지에 달린 리액션을 reaction별 count로 반환합니다.")
    @GetMapping("/{id}/reactions")
    suspend fun getReactions(@PathVariable("id") messageId: Long): ResponseEntity<List<ReactionSummaryItem>> {
        return ResponseEntity.ok(getReactionsSummaryUseCase(messageId))
    }
}
