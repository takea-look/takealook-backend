package com.takealook.storage

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

data class PresignRequest(
    val roomId: Long,
    val contentType: String,
    val sizeBytes: Long? = null,
)

data class PresignResponse(
    val url: String,
    val key: String,
    val headers: Map<String, String>,
)

@Tag(name = "Storage", description = "스토리지 관리 API")
@RestController
class UploadPresignController(
    private val storageService: StorageService,
) {

    @Operation(summary = "이미지 업로드 presigned URL 생성", description = "채팅용 이미지 업로드에 사용할 presigned PUT URL을 발급합니다.")
    @PostMapping("/uploads/presign")
    fun presignImageUpload(@RequestBody body: PresignRequest): ResponseEntity<PresignResponse> {
        val key = storageService.generateChatMessageUploadKey(body.roomId, body.contentType)
        val url = storageService.generateUploadUrl(
            key = key,
            sizeBytes = body.sizeBytes,
            contentType = body.contentType,
        )
        return ResponseEntity.ok(
            PresignResponse(
                url = url,
                key = key,
                headers = mapOf("Content-Type" to body.contentType),
            )
        )
    }
}
