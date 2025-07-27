package com.takealook.stickers.stickers

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface StickerRepository : CoroutineCrudRepository<Sticker, Int> {
    /**
     * Find all icons by category id.
     */
    suspend fun findByCategoryId(categoryId: Long): Flow<Sticker>
}
