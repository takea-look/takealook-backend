package com.takealook.stickers.stickers

import com.takealook.domain.sticker.GetStickersUseCase
import com.takealook.domain.sticker.SaveStickerUseCase
import com.takealook.model.Sticker
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Stickers", description = "스티커 관리 API")
@RestController
@RequestMapping("/stickers")
class StickerController @Autowired constructor(
    private val getStickersUseCase: GetStickersUseCase,
    private val saveStickerUseCase: SaveStickerUseCase
) {

    @Operation(summary = "스티커 생성", description = "새로운 스티커를 생성합니다.")
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

    @Operation(summary = "스티커 목록 조회", description = "스티커 목록을 조회합니다. 카테고리 ID로 필터링이 가능합니다.")
    @GetMapping
    suspend fun getIcons(
        @RequestParam(required = false) categoryId: Long?
    ): ResponseEntity<List<Sticker>> {
        val stickers = getStickersUseCase(categoryId)
        return ResponseEntity.ok(stickers.toList())
    }
}
