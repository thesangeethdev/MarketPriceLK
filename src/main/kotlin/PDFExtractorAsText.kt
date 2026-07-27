package com.sangeeth

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

fun extractTextFromPdf(pdfBytes: ByteArray): String {

    return Loader.loadPDF(pdfBytes).use { document ->
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

//fun extractTextFromPdf(filepath: String): String {
//    val bytes = File(filepath).readBytes()
//    return extractTextFromPdf(bytes)
//}