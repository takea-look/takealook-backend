package com.takealook.domain.sticker

import com.takealook.data.sticker.StickerRepository
import org.springframework.stereotype.Component

@Component
class SaveStickerUseCase(
    private val repository: StickerRepository
) {
    suspend operator fun invoke(
        sticker: Sticker,
    ): Sticker = repository
        .save(sticker.toStickerEntity())
        .toSticker()
}
