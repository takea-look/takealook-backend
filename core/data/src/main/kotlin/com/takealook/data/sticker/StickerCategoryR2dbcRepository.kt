package com.takealook.data.sticker

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StickerCategoryR2dbcRepository : CoroutineCrudRepository<StickerCategoryEntity, Int>
