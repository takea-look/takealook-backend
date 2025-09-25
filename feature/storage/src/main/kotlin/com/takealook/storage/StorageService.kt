package com.takealook.storage

import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Service
class StorageService(
    private val props: StorageProps,
    private val s3Presigner: S3Presigner,
) {
    fun generateUploadUrl(key: String): String {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(props.bucket)
            .key(key)
            .build()


        val presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .putObjectRequest(putObjectRequest)
            .build()

        return s3Presigner.presignPutObject(presignRequest).url().toString()
    }
}
