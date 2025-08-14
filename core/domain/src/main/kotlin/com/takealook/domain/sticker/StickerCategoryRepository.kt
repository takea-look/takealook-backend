package com.takealook.domain.sticker

import com.takealook.model.StickerCategory

interface StickerCategoryRepository {
    suspend fun saveStickerCategory(category: StickerCategory): StickerCategory
    suspend fun findAllCategories(): List<StickerCategory>
}
