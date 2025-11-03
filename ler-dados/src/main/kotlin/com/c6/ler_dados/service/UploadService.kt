package com.c6.ler_dados.service

import com.c6.ler_dados.model.FileEntity
import com.c6.ler_dados.repository.FileRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class UploadService(
    private val repository: FileRepository,
    private val s3UploaderService: S3UploaderService
) {
    private val logger = LoggerFactory.getLogger(UploadService::class.java)

    fun upload(file: MultipartFile): FileEntity {
        logger.info("Starting file upload process for '{}'", file.originalFilename)

        try {
            val fileName = file.originalFilename ?: "unnamed-file"
            val tempFile = createTempFile(fileName)
            file.transferTo(tempFile)

            val s3Url = s3UploaderService.uploadFile(tempFile)
            logger.info("File '{}' successfully uploaded to S3 at {}", fileName, s3Url)

            val entity = FileEntity(
                fileName = fileName,
                s3Path = s3Url,
                uploadDate = LocalDateTime.now()
            )

            val savedEntity = repository.save(entity)
            logger.info("File '{}' successfully saved in the database with ID {}", fileName, savedEntity.id)

            tempFile.delete()
            return savedEntity
        } catch (e: Exception) {
            logger.error("Error during file upload process: {}", e.message)
            throw e
        }
    }

    private fun createTempFile(originalName: String): java.io.File {
        val sanitized = originalName.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
        val tempFile = java.io.File.createTempFile("upload_", "_$sanitized")
        logger.debug("Temporary file created: {}", tempFile.absolutePath)
        return tempFile
    }
}
