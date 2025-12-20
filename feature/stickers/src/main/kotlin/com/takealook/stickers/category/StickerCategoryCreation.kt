package com.takealook.stickers.category

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "스티커 카테고리 생성 요청")
data class StickerCategoryCreation(
    @Schema(description = "카테고리 이름", example = "인기 스티커")
    val name: String,
    @Schema(description = "카테고리 썸네일 URL", example = "http://example.com/category-thumb.png")
    val thumbnailUrl: String
)
