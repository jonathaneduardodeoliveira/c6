package com.c6.ler_dados.controller

import com.c6.ler_dados.model.FileEntity
import com.c6.ler_dados.repository.FileRepository
import com.c6.ler_dados.service.UploadService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api")
class UploadController(
    private val uploadService: UploadService,
    private val repository: FileRepository
) {
    @PostMapping("/upload")
    fun upload(@RequestParam("file") file: MultipartFile): ResponseEntity<FileEntity> {
        val result = uploadService.upload(file)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/files")
    fun listAll(): ResponseEntity<List<FileEntity>> {
        val files = repository.findAll().sortedByDescending { it.uploadDate }
        return ResponseEntity.ok(files)
    }

    @GetMapping("/files/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<FileEntity> {
        val file = repository.findById(id)
        return if (file.isPresent) ResponseEntity.ok(file.get()) else ResponseEntity.notFound().build()
    }

    @GetMapping("/files/search")
    fun searchByName(@RequestParam("q") name: String): ResponseEntity<List<FileEntity>> {
        val results = repository.findByPersonNameContainingIgnoreCase(name)
        return ResponseEntity.ok(results)
    }
}
