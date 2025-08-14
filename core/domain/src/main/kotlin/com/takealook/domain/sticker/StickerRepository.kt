package com.takealook.domain.sticker

import com.takealook.model.Sticker

interface StickerRepository {
    suspend fun saveSticker(sticker: Sticker): Sticker
    suspend fun findStickerByCategoryId(categoryId: Long): List<Sticker>
    suspend fun findAllStickers(): List<Sticker>
}
