package com.takealook.storage

import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Storage", description = "스토리지 관리 API")
@RestController
@RequestMapping("/storage")
class StorageController(
    private val service: StorageService,
    private val meterRegistry: MeterRegistry,
) {

    @Operation(summary = "Presigned URL 생성", description = "키 기반으로 업로드용 Presigned URL을 생성합니다.")
    @GetMapping("/upload")
    fun getUploadUrl(
        @RequestParam key: String,
        @RequestParam(required = false) sizeBytes: Long?,
        @RequestParam(required = false) contentType: String?,
    ): ResponseEntity<StorageService.UploadResponse> {
        meterRegistry.counter("takealook_upload_url_requests_total", "outcome", "request").increment()
        return ResponseEntity.ok(service.uploadResponse(key = key, sizeBytes = sizeBytes, contentType = contentType))
    }
}
