package com.takealook.stickers.category

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface StickerCategoryRepository : CoroutineCrudRepository<StickerCategory, Int>
