package com.takealook.stickers.stickers

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("stickers")
data class Sticker(
    @Id val id: Int? = null,
    val name: String,
    val iconUrl: String,
    val thumbnailUrl: String,
    val categoryId: Int
)
