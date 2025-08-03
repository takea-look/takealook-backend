package com.takealook.data.sticker

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface StickerCategoryRepository : CoroutineCrudRepository<StickerCategoryEntity, Int>
