package com.takealook.data.sticker

import com.takealook.model.Sticker
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("stickers")
data class StickerEntity(
    @Id val id: Int? = null,
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