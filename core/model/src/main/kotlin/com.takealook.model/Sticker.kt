package com.takealook.domain.sticker

import com.takealook.data.sticker.StickerEntity

data class Sticker(
    val id: Int? = null,
    val name: String,
    val iconUrl: String,
    val thumbnailUrl: String,
    val categoryId: Int
)

fun StickerEntity.toSticker() = Sticker(
    id = id,
    name = name,
    iconUrl = iconUrl,
    thumbnailUrl = thumbnailUrl,
    categoryId = categoryId
)

fun Sticker.toStickerEntity() = StickerEntity(
    id = id,
    name = name,
    iconUrl = iconUrl,
    thumbnailUrl = thumbnailUrl,
    categoryId = categoryId
)

