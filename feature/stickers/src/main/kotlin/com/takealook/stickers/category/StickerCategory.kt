package com.takealook.stickers.category

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("sticker_categories")
data class StickerCategory(
    @Id val id: Int? = null,
    val name: String,
    val thumbnailUrl: String
)
