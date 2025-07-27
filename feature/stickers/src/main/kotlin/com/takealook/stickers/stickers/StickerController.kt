package com.takealook.stickers.stickers

import kotlinx.coroutines.flow.toList
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
    private val iconRepository: StickerRepository
) {

    @PostMapping
    suspend fun createIcon(@RequestBody request: StickerCreation): ResponseEntity<Sticker> {
        val icon = Sticker(
            name = request.name,
            iconUrl = request.iconUrl,
            thumbnailUrl = request.thumbnailUrl,
            categoryId = request.categoryId
        )
        val savedIcon = iconRepository.save(icon)
        return ResponseEntity.ok(savedIcon)
    }

    @GetMapping
    suspend fun getIcons(
        @RequestParam(required = false) categoryId: Long?
    ): ResponseEntity<List<Sticker>> {
        val icons = if (categoryId != null) {
            iconRepository.findByCategoryId(categoryId)
        } else {
            iconRepository.findAll()
        }
        return ResponseEntity.ok(icons.toList())
    }
}
