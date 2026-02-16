package com.takealook.storage

import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Instant
import java.time.Duration

@Service
class StorageService(
    private val props: StorageProps,
    private val s3Presigner: S3Presigner,
) {
    fun generateUploadUrl(
        key: String,
        sizeBytes: Long? = null,
        contentType: String? = null,
    ): String {
        validateKey(key)
        validateSize(sizeBytes)
        validateContentTypeWithKey(key, contentType)

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(props.bucket)
            .key(key)
            .apply {
                if (!contentType.isNullOrBlank()) {
                    contentType(contentType)
                }
            }
            .build()

        val presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(props.presignTtlMinutes))
            .putObjectRequest(putObjectRequest)
            .build()

        return s3Presigner.presignPutObject(presignRequest).url().toString()
    }

    fun generateChatMessageUploadKey(roomId: Long, mimeType: String): String {
        validateRoomId(roomId)
        val ext = extensionForMime(mimeType)
        val timestamp = Instant.now().toEpochMilli()
        return "${props.allowedKeyPrefix}$roomId/$timestamp.$ext"
    }

    fun canonicalImageUrl(key: String): String {
        return "${props.publicBaseUrl.trimEnd('/')}/$key"
    }

    fun validateChatUploadKeyAndSize(key: String, sizeBytes: Long?) {
        validateKey(key)
        validateSize(sizeBytes)
    }

    fun validateContentTypeWithKey(key: String, contentType: String?) {
        if (contentType.isNullOrBlank()) return
        val normalized = contentType.lowercase().trim().substringBefore(';').trim()
        val ext = extensionForMime(normalized)
        val keyExt = key.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        if (keyExt != ext) {
            throw IllegalArgumentException("contentType and key extension mismatch")
        }
    }

    private fun validateRoomId(roomId: Long) {
        if (roomId <= 0) {
            throw IllegalArgumentException("roomId must be greater than 0")
        }
    }

    fun extensionForMime(mimeType: String): String {
        val normalized = mimeType.lowercase().trim().substringBefore(';').trim()
        return when (normalized) {
            "image/png" -> "png"
            "image/jpeg" -> "jpeg"
            "image/jpg" -> "jpg"
            "image/webp" -> "webp"
            else -> throw IllegalArgumentException("Unsupported mime type: $mimeType")
        }
    }

    private fun validateKey(key: String) {
        if (key.isBlank()) throw IllegalArgumentException("key is required")
        if (!key.startsWith(props.allowedKeyPrefix)) {
            throw IllegalArgumentException("key must start with '${props.allowedKeyPrefix}'")
        }

        val ext = key.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        if (ext.isBlank() || ext !in props.allowedExtensions.map { it.lowercase() }) {
            throw IllegalArgumentException("extension not allowed: .$ext")
        }

        // Simple policy: chat/{roomId}/{timestamp}.{ext}
        val withoutPrefix = key.removePrefix(props.allowedKeyPrefix)
        val parts = withoutPrefix.split('/')
        if (parts.size != 2) {
            throw IllegalArgumentException("invalid key format; expected chat/{roomId}/{timestamp}.{ext}")
        }
        val roomId = parts[0]
        if (roomId.toLongOrNull() == null) {
            throw IllegalArgumentException("invalid roomId in key")
        }
        val filename = parts[1].substringBeforeLast('.', missingDelimiterValue = "")
        if (filename.toLongOrNull() == null) {
            throw IllegalArgumentException("invalid timestamp filename in key")
        }
    }

    private fun validateSize(sizeBytes: Long?) {
        if (sizeBytes == null) return
        if (sizeBytes <= 0) throw IllegalArgumentException("sizeBytes must be > 0")
        if (sizeBytes > props.maxUploadBytes) {
            throw IllegalArgumentException("sizeBytes exceeds maxUploadBytes(${props.maxUploadBytes})")
        }
    }

    fun uploadResponse(
        key: String,
        sizeBytes: Long? = null,
        contentType: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): UploadResponse {
        val finalHeaders = headers.toMutableMap()
        if (!contentType.isNullOrBlank()) {
            finalHeaders["Content-Type"] = contentType
        }

        return UploadResponse(
            url = generateUploadUrl(key, sizeBytes, contentType),
            key = key,
            canonicalUrl = canonicalImageUrl(key),
            headers = finalHeaders,
            maxUploadBytes = props.maxUploadBytes,
            expiresInSeconds = props.presignTtlMinutes * 60,
        )
    }

    data class UploadResponse(
        val url: String,
        val key: String,
        val canonicalUrl: String,
        val headers: Map<String, String>,
        val maxUploadBytes: Long,
        val expiresInSeconds: Long,
    )
}
