package com.takealook.data.sticker

import com.takealook.model.StickerCategory
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("sticker_categories")
data class StickerCategoryEntity(
    @Id val id: Int? = null,
    val name: String,
    val thumbnailUrl: String
)

fun StickerCategoryEntity.toStickerCategory() = StickerCategory(
    id = id,
    name = name,
    thumbnailUrl = thumbnailUrl,
)

fun StickerCategory.toStickerCategoryEntity() = StickerCategoryEntity(
    id = id,
    name = name,
    thumbnailUrl = thumbnailUrl,
)