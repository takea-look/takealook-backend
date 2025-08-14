package com.takealook.domain.sticker
import com.takealook.model.Sticker

class GetStickersUseCase(
    private val repository: StickerRepository
) {
    suspend operator fun invoke(
        categoryId: Long? = null,
    ): List<Sticker> {
        val result = if (categoryId != null) {
            repository.findStickerByCategoryId(categoryId)
        } else {
            repository.findAllStickers()
        }
        return result
    }
}
