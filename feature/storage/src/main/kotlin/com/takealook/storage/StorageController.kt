package com.takealook.storage

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Storage", description = "스토리지 관리 API")
@RestController
@RequestMapping("/storage")
class StorageController(
    private val service: StorageService
) {

    @Operation(summary = "업로드 URL 생성", description = "파일 업로드를 위한 Pre-signed URL을 생성합니다.")
    @GetMapping("/upload")
    fun getUploadUrl(@RequestParam key: String) =
        mapOf("url" to service.generateUploadUrl(key))
}