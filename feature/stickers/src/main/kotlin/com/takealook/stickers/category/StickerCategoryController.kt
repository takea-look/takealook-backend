package com.takealook.stickers.category

import com.takealook.domain.sticker.GetStickerCategoriesUseCase
import com.takealook.domain.sticker.SaveStickerCategoryUseCase
import com.takealook.model.StickerCategory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Sticker Categories", description = "스티커 카테고리 관리 API")
@RestController
@RequestMapping("/sticker-categories")
class StickerCategoryController @Autowired constructor(
    private val getStickerCategoriesUseCase: GetStickerCategoriesUseCase,
    private val saveStickerCategoryUseCase: SaveStickerCategoryUseCase,
) {

    @Operation(summary = "스티커 카테고리 생성", description = "새로운 스티커 카테고리를 생성합니다.")
    @PostMapping
    suspend fun createCategory(@RequestBody request: StickerCategoryCreation): ResponseEntity<StickerCategory> {
        val category = StickerCategory(name = request.name, thumbnailUrl = request.thumbnailUrl)
        val savedCategory = saveStickerCategoryUseCase(category)
        return ResponseEntity.ok(savedCategory)
    }

    @Operation(summary = "스티커 카테고리 목록 조회", description = "모든 스티커 카테고리 목록을 조회합니다.")
    @GetMapping
    suspend fun getAllCategories(): ResponseEntity<List<StickerCategory>> {
        val categories = getStickerCategoriesUseCase()
        return ResponseEntity.ok(categories.toList())
    }
}
