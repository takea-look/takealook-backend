package com.takealook.domain.sticker

import com.takealook.data.sticker.StickerCategoryEntity
import com.takealook.data.sticker.StickerCategoryRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class GetStickerCategoriesUseCase(
    private val repository: StickerCategoryRepository
) {
    suspend operator fun invoke(
        categoryId: Long? = null,
    ): List<StickerCategory> = repository
        .findAll()
        .map(StickerCategoryEntity::toStickerCategory)
        .toList()
}
