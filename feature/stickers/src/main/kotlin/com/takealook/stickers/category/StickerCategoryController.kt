package com.takealook.stickers.category

import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/icon-categories")
class StickerCategoryController @Autowired constructor(
    private val stickerCategoryRepository: StickerCategoryRepository
) {

    @PostMapping
    suspend fun createCategory(@RequestBody request: StickerCategoryCreation): ResponseEntity<StickerCategory> {
        val category = StickerCategory(name = request.name, thumbnailUrl = request.thumbnailUrl)
        val savedCategory = stickerCategoryRepository.save(category)
        return ResponseEntity.ok(savedCategory)
    }

    @GetMapping
    suspend fun getAllCategories(): ResponseEntity<List<StickerCategory>> {
        val categories = stickerCategoryRepository.findAll()
        return ResponseEntity.ok(categories.toList())
    }
}
