package com.takealook.domain.sticker

import com.takealook.model.Sticker

class SaveStickerUseCase(
    private val repository: StickerRepository
) {
    suspend operator fun invoke(
        sticker: Sticker,
    ): Sticker = repository.saveSticker(sticker)
}
