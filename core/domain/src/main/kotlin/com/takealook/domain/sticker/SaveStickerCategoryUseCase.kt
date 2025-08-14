package com.takealook.domain.sticker

import com.takealook.model.StickerCategory

class SaveStickerCategoryUseCase(
    private val repository: StickerCategoryRepository
) {
    suspend operator fun invoke(
        category: StickerCategory,
    ): StickerCategory = repository.saveStickerCategory(category)
}
