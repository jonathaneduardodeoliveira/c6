package com.c6.ler_dados.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "files", indexes = [Index(name = "idx_filename", columnList = "file_name")])
data class FileEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true, name = "file_name")
    var fileName: String,

    @Column(name = "file_type")
    var fileType: String? = null,

    @Column(name = "s3_path")
    var s3Path: String? = null,

    @Column(name = "person_name")
    var personName: String? = null,

    @Column(name = "cnpj")
    var cnpj: String? = null,

    @Column(name = "address")
    var address: String? = null,

    @Column(name = "credit_risk")
    var creditRisk: String? = null,

    @Lob
    @Column(name = "extracted_text")
    var extractedText: String? = null,

    @Column(name = "upload_date")
    var uploadDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_date")
    var updatedDate: LocalDateTime? = null
)
