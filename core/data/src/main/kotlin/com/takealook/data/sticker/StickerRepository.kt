package com.takealook.data.sticker

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface StickerRepository : CoroutineCrudRepository<StickerEntity, Int> {
    /**
     * Find all stickers by category id.
     */
    suspend fun findByCategoryId(categoryId: Long): Flow<StickerEntity>
}
