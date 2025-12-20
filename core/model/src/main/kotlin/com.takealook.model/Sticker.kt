package com.takealook.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "스티커 정보")
data class Sticker(
    @Schema(description = "스티커 ID", example = "1")
    val id: Int? = null,
    @Schema(description = "스티커 이름", example = "smile")
    val name: String,
    @Schema(description = "스티커 아이콘 URL", example = "http://example.com/icon.png")
    val iconUrl: String,
    @Schema(description = "스티커 썸네일 URL", example = "http://example.com/thumb.png")
    val thumbnailUrl: String,
    @Schema(description = "카테고리 ID", example = "1")
    val categoryId: Int
)


