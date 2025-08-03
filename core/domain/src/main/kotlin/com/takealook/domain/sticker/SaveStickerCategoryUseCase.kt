package com.takealook.domain.sticker

import com.takealook.data.sticker.StickerCategoryRepository
import org.springframework.stereotype.Component

@Component
class SaveStickerCategoryUseCase(
    private val repository: StickerCategoryRepository
) {
    suspend operator fun invoke(
        category: StickerCategory,
    ): StickerCategory = repository
        .save(category.toStickerCategoryEntity())
        .toStickerCategory()
}
