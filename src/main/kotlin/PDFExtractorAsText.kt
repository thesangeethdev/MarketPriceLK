package com.sangeeth

import ai.koog.rag.base.files.model.FileSystemEntry
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

fun extractTextFromPdf(filepath: String): String {
    val document : PDDocument = Loader.loadPDF(File(filepath))
    return document.use { document ->
        PDFTextStripper().getText(document)
    }
}