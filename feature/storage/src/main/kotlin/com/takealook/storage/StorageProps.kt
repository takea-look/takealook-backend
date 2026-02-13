package com.takealook.storage

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(StorageProps::class)
class PropertiesConfig

@ConfigurationProperties(prefix = "cloud.r2")
data class StorageProps @ConstructorBinding constructor(
    val accountId: String,
    val accessKey: String,
    val secretKey: String,
    val bucket: String,
    val region: String,
    val presignTtlMinutes: Long = 10,
    val maxUploadBytes: Long = 10L * 1024 * 1024, // 10MB
    val allowedExtensions: List<String> = listOf("png", "jpg", "jpeg", "webp"),
    val allowedKeyPrefix: String = "chat/",
)
