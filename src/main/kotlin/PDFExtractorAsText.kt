package com.sangeeth

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

fun extractTextFromPdf(filepath: String): String {
    val document : PDDocument = Loader.loadPDF(File(filepath))
    return document.use { document ->
        var stripper = PDFTextStripper().apply {
            sortByPosition = true
//            startPage = 2
        }
        val raw = stripper.getText(document)
        val cleaned = raw.replace(Regex("""(?<=\d)\s+(?=\d)"""), "")
        cleaned
    }


//    val document : PDDocument = Loader.loadPDF(File(filepath))
//    return document.use { document ->
//        var stripper = PDFTextStripper().apply {
//            sortByPosition = true
//            startPage = 2
//        }
//        val raw = stripper.getText(document)
//        val cleaned = raw.replace(Regex("""(\d)\s+(\d{2}\.\d{2})"""), "$1$2")
//        cleaned
//    }


}