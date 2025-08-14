package com.takealook.data.sticker

import com.takealook.domain.sticker.StickerRepository
import com.takealook.model.Sticker
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Repository

@Repository
class StickerRepositoryImpl(
    private val repository: StickerR2dbcRepository
) : StickerRepository {
    override suspend fun saveSticker(sticker: Sticker): Sticker =
        repository.save(sticker.toStickerEntity()).toSticker()

    override suspend fun findStickerByCategoryId(categoryId: Long): List<Sticker> = repository
        .findByCategoryId(categoryId)
        .map(StickerEntity::toSticker)
        .toList()

    override suspend fun findAllStickers(): List<Sticker> = repository
        .findAll()
        .map(StickerEntity::toSticker)
        .toList()
}
