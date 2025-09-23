package com.takealook.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Configuration
class StorageConfig {

    @Value("\${cloud.r2.account-id}")
    private lateinit var accountId: String

    @Value("\${cloud.r2.access-key}")
    private lateinit var accessKey: String

    @Value("\${cloud.r2.secret-key}")
    private lateinit var secretKey: String

    @Value("\${cloud.r2.bucket}")
    private lateinit var bucket: String

    @Value("\${cloud.r2.region}")
    private lateinit var region: String

    private fun endpoint() = "https://$accountId.r2.cloudflarestorage.com"

    @Bean
    fun s3Client(): S3Client = S3Client.builder()
        .endpointOverride(URI.create(endpoint()))
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            )
        )
        .build()
}