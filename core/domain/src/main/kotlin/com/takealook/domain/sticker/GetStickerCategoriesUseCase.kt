package com.takealook.domain.sticker

import com.takealook.model.StickerCategory

class GetStickerCategoriesUseCase(
    private val repository: StickerCategoryRepository
) {
    suspend operator fun invoke(
        categoryId: Long? = null,
    ): List<StickerCategory> = repository.findAllCategories()
}
