package com.c6.ler_dados.service

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.tika.Tika
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.util.regex.Pattern

@Component
class DocumentParser {
    private val logger = LoggerFactory.getLogger(DocumentParser::class.java)
    private val tika = Tika()

    fun extractText(file: File): String {
        logger.info("Extracting text from file '{}'", file.name)
        return try {
            val content = when {
                file.name.endsWith(".docx", true) -> XWPFDocument(file.inputStream()).use {
                    it.paragraphs.joinToString("\n") { p -> p.text }
                }
                file.name.endsWith(".xlsx", true) -> XSSFWorkbook(file).use { workbook ->
                    val builder = StringBuilder()
                    workbook.forEach { sheet ->
                        sheet.forEach { row ->
                            row.forEach { cell -> builder.append("${cell.toString()} | ") }
                            builder.append("\n")
                        }
                    }
                    builder.toString()
                }
                else -> tika.parseToString(file)
            }
            logger.info("Text successfully extracted from '{}'", file.name)
            logger.debug("Extracted content (first 100 chars): {}", content.take(100))
            content
        } catch (e: Exception) {
            logger.error("Error extracting text from '{}': {}", file.name, e.message)
            throw e
        }
    }

    fun extractCnpj(text: String): String? {
        logger.debug("Extracting CNPJ pattern from text")
        val pattern = Pattern.compile("\\b\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}\\b|\\b\\d{14}\\b")
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group().trim() else null
    }

    fun extractName(text: String): String? {
        logger.debug("Extracting company name from text")
        val patterns = listOf("Name:\\s*(.+)", "Corporate Name:\\s*(.+)", "Company Name:\\s*(.+)")
        for (pt in patterns) {
            val pattern = Pattern.compile(pt, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(text)
            if (matcher.find()) return matcher.group(1).split("\n")[0].trim()
        }
        return text.lines().firstOrNull { it.isNotBlank() }?.trim()
    }

    fun extractAddress(text: String): String? {
        logger.debug("Extracting address from text")
        val pattern = Pattern.compile(
            "(Address:\\s*.+|Street\\s+.+|Av\\.\\s+.+|Avenue\\s+.+)",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group().trim() else null
    }

    fun extractCreditRisk(text: String): String? {
        logger.debug("Extracting credit risk or score from text")
        val pattern = Pattern.compile(
            "(Risk\\s*[:\\-]\\s*\\w+|Score\\s*[:\\-]\\s*\\d{2,4})",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group().trim() else null
    }
}
