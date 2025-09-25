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
    val region: String
)
