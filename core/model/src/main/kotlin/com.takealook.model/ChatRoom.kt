package com.takealook.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "채팅방 정보")
data class ChatRoom(
    @Schema(description = "채팅방 ID", example = "1")
    val id: Long? = null,
    @Schema(description = "채팅방 이름", example = "점심 메뉴 정하기")
    val name: String,
    @Schema(description = "공개 여부", example = "true")
    val isPublic: Boolean,
    @Schema(description = "최대 참여 인원", example = "10")
    val maxParticipants: Int,
    @Schema(description = "생성일시 (타임스탬프)", example = "1672531200000")
    val createdAt: Long,
)
