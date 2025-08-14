package com.takealook.data.sticker

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StickerR2dbcRepository : CoroutineCrudRepository<StickerEntity, Int> {
    /**
     * Find all stickers by category id.
     */
    abstract suspend fun findByCategoryId(categoryId: Long): Flow<StickerEntity>
}
