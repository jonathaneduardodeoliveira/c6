package com.c6.ler_dados.repository

import com.c6.ler_dados.model.FileEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FileRepository : JpaRepository<FileEntity, Long> {
    fun findByFileName(fileName: String): FileEntity?
    fun findByPersonNameContainingIgnoreCase(personName: String): List<FileEntity>
}
