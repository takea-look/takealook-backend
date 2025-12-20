package com.takealook.stickers.stickers

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "스티커 생성 요청")
data class StickerCreation(
    @Schema(description = "스티커 이름", example = "smile")
    val name: String,
    @Schema(description = "스티커 아이콘 URL", example = "http://example.com/icon.png")
    val iconUrl: String,
    @Schema(description = "스티커 썸네일 URL", example = "http://example.com/thumb.png")
    val thumbnailUrl: String,
    @Schema(description = "카테고리 ID", example = "1")
    val categoryId: Int
)
