package com.takealook.domain.sticker

import com.takealook.data.sticker.StickerEntity
import com.takealook.data.sticker.StickerRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class GetStickersUseCase(
    private val repository: StickerRepository
) {
    suspend operator fun invoke(
        categoryId: Long? = null,
    ): List<Sticker> {
        val result = if (categoryId != null) {
            repository.findByCategoryId(categoryId).toList()
        } else {
            repository.findAll().toList()
        }
        return result.map(StickerEntity::toSticker)
    }
}
