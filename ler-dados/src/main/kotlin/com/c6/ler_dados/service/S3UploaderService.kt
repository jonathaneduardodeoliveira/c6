package com.c6.ler_dados.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.io.File
import java.net.URI

@Service
class S3UploaderService(
    @Value("\${AWS_S3_BUCKET}") private val bucket: String,
    @Value("\${AWS_ENDPOINT}") private val endpoint: String,
    @Value("\${AWS_REGION}") private val region: String,
    @Value("\${AWS_ACCESS_KEY_ID}") private val accessKey: String,
    @Value("\${AWS_SECRET_ACCESS_KEY}") private val secretKey: String
) {
    private val logger = LoggerFactory.getLogger(S3UploaderService::class.java)

    private val s3Client: S3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
        )
        // ✅ Força o modo PATH-style (impede "dados-local.localstack")
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
        .endpointOverride(URI.create(endpoint))
        .build()

    fun uploadFile(file: File, overwrite: Boolean = false): String {
        logger.info("Starting upload for file '{}'", file.name)
        try {
            createBucketIfMissing()
            val exists = fileExists(file.name)

            if (!exists || overwrite) {
                val request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(file.name)
                    .build()

                s3Client.putObject(request, file.toPath())
                logger.info("File '{}' successfully uploaded to bucket '{}'", file.name, bucket)
            } else {
                logger.warn("File '{}' already exists in bucket '{}', skipping upload", file.name, bucket)
            }

            val s3Path = "s3://$bucket/${file.name}"
            logger.debug("File available at '{}'", s3Path)
            return s3Path
        } catch (e: Exception) {
            logger.error("Error uploading file '{}': {}", file.name, e.message)
            throw e
        }
    }

    private fun createBucketIfMissing() {
        val exists = s3Client.listBuckets().buckets().any { it.name() == bucket }
        if (!exists) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build())
            logger.info("Bucket '{}' created successfully", bucket)
        } else {
            logger.debug("Bucket '{}' already exists", bucket)
        }
    }

    private fun fileExists(fileName: String): Boolean {
        val response = s3Client.listObjectsV2(
            ListObjectsV2Request.builder().bucket(bucket).build()
        )
        val exists = response.contents().any { it.key() == fileName }
        logger.debug("File '{}' exists in bucket '{}': {}", fileName, bucket, exists)
        return exists
    }
}
