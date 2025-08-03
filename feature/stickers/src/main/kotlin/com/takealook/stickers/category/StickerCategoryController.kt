package com.takealook.stickers.category

import com.takealook.domain.sticker.GetStickerCategoriesUseCase
import com.takealook.domain.sticker.SaveStickerCategoryUseCase
import com.takealook.domain.sticker.StickerCategory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sticker-categories")
class StickerCategoryController @Autowired constructor(
    private val getStickerCategoriesUseCase: GetStickerCategoriesUseCase,
    private val saveStickerCategoryUseCase: SaveStickerCategoryUseCase,
) {

    @PostMapping
    suspend fun createCategory(@RequestBody request: StickerCategoryCreation): ResponseEntity<StickerCategory> {
        val category = StickerCategory(name = request.name, thumbnailUrl = request.thumbnailUrl)
        val savedCategory = saveStickerCategoryUseCase(category)
        return ResponseEntity.ok(savedCategory)
    }

    @GetMapping
    suspend fun getAllCategories(): ResponseEntity<List<StickerCategory>> {
        val categories = getStickerCategoriesUseCase()
        return ResponseEntity.ok(categories.toList())
    }
}
