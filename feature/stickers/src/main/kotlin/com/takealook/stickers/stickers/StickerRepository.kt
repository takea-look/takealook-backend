package com.takealook.stickers.stickers

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface StickerRepository : CoroutineCrudRepository<Sticker, Int>
