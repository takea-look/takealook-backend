package com.takealook.stickers.stickers

import com.takealook.domain.sticker.GetStickersUseCase
import com.takealook.domain.sticker.SaveStickerUseCase
import com.takealook.model.Sticker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/stickers")
class StickerController @Autowired constructor(
    private val getStickersUseCase: GetStickersUseCase,
    private val saveStickerUseCase: SaveStickerUseCase
) {

    @PostMapping
    suspend fun createIcon(@RequestBody request: StickerCreation): ResponseEntity<Sticker> {
        val sticker = Sticker(
            name = request.name,
            iconUrl = request.iconUrl,
            thumbnailUrl = request.thumbnailUrl,
            categoryId = request.categoryId
        )
        val savedIcon = saveStickerUseCase(sticker)
        return ResponseEntity.ok(savedIcon)
    }

    @GetMapping
    suspend fun getIcons(
        @RequestParam(required = false) categoryId: Long?
    ): ResponseEntity<List<Sticker>> {
        val stickers = getStickersUseCase(categoryId)
        return ResponseEntity.ok(stickers.toList())
    }
}
