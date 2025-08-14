package com.takealook.data.sticker

import com.takealook.domain.sticker.StickerCategoryRepository
import com.takealook.model.StickerCategory
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Repository

@Repository
class StickerCategoryRepositoryImpl(
    private val repository: StickerCategoryR2dbcRepository,
) : StickerCategoryRepository {
    override suspend fun saveStickerCategory(category: StickerCategory): StickerCategory {
        return repository.save(category.toStickerCategoryEntity()).toStickerCategory()
    }

    override suspend fun findAllCategories(): List<StickerCategory> {
        return repository.findAll()
            .map(StickerCategoryEntity::toStickerCategory)
            .toList()
    }
}
