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

    fun validateChatUploadKeyAndSize(key: String, sizeBytes: Long?) {
        validateKey(key)
        validateSize(sizeBytes)
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
}
