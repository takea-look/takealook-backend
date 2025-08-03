package com.takealook.domain.sticker

import com.takealook.data.sticker.StickerCategoryEntity

data class StickerCategory(
    val id: Int? = null,
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