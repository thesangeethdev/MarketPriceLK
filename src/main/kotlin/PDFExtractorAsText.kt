package com.sangeeth

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
//import java.io.File

//fun extractTextFromPdf(pdfBytes: ByteArray): String {
//
//    return Loader.loadPDF(pdfBytes).use { document ->
//        var stripper = PDFTextStripper().apply {
//            sortByPosition = true
////            startPage = 2
//        }
//        val raw = stripper.getText(document)
//        val cleaned = raw.replace(Regex("""(?<=\d)\s+(?=\d)"""), "")
//        cleaned
//    }
//}
//
//fun extractTextFromPdf(filepath: String): String {
//    val bytes = File(filepath).readBytes()
//    return extractTextFromPdf(bytes)
//}

//import java.io.File
//import kotlinx.coroutines.runBlocking
//
//// ===================== PDF EXTRACTION (UPDATED) =====================
//
//fun extractTextFromPdf(pdfBytes: ByteArray): String {
//    return Loader.loadPDF(pdfBytes).use { document ->
//        PDFTextStripper().apply {
//            sortByPosition = true
//            // Uncomment the next line if you want to skip the summary page entirely
//            // startPage = 2
//        }.getText(document).let { raw ->
//            cleanExtractedText(raw)
//        }
//    }
//}
//
//fun extractTextFromPdf(filepath: String): String {
//    val bytes = File(filepath).readBytes()
//    return extractTextFromPdf(bytes)
//}
//
///**
// * Cleans the raw PDF text:
// * - Removes the long date‑list footer
// * - Collapses multiple spaces/newlines
// * - Inserts spaces between glued numbers (e.g., 1,700.001,900.00 → 1,700.00 1,900.00)
// * - Normalises "n.a."
// */
//private fun cleanExtractedText(raw: String): String {
//    // 1. Drop the footer lines that are only dates
//    val lines = raw.lines()
//        .filter { line ->
//            !line.matches(Regex("""^\s*(\d{1,2}-[A-Za-z]{3}\s+)+$""")) &&
//                    !line.matches(Regex("""^\s*\d{1,2}-[A-Za-z]{3}\s+\d{1,2}-[A-Za-z]{3}""")) &&
//                    line.isNotBlank()
//        }
//        .joinToString("\n")
//
//    // 2. Collapse all whitespace into single spaces
//    var cleaned = lines.replace(Regex("\\s+"), " ")
//
//    // 3. Fix concatenated numbers: e.g., "1,700.001,900.00" → "1,700.00 1,900.00"
//    cleaned = cleaned.replace(Regex("""(\d+(?:,\d+)?\.\d{2})(\d+(?:,\d+)?\.\d{2})"""), "$1 $2")
//
//    // 4. Normalise "n.a."
//    cleaned = cleaned.replace(Regex("""n\.a\.""", RegexOption.IGNORE_CASE), "n.a.")
//
//    // 5. Final clean‑up
//    cleaned = cleaned.replace(Regex("\\s+"), " ").trim()
//
//    return cleaned
//}
//
//// ===================== LOCAL TEST =====================
//
//fun main() = runBlocking {
//    val pdfPath = "price_report_20260722_e.pdf"   // Change to your file name
//    val rawText = extractTextFromPdf(pdfPath)
//
//    // Save the full cleaned text for inspection
//    File("full_raw_text_cleaned.txt").writeText(rawText)
//    println("✅ Cleaned full text written to full_raw_text_cleaned.txt (length: ${rawText.length})")
//
//    // Extract the Fish section (case‑insensitive)
//    val fishStart = rawText.indexOf("FISH", ignoreCase = true)
//    if (fishStart == -1) {
//        println("❌ Could not find 'FISH' in the extracted text. Dumping first 500 chars:")
//        println(rawText.take(500))
//        return@runBlocking
//    }
//
//    val fishEnd = rawText.indexOf("RICE", fishStart, ignoreCase = true)
//    val fishRaw = rawText.substring(fishStart, if (fishEnd != -1) fishEnd else rawText.length)
//
//    println("\n=== 🐟 FISH RAW TEXT (CLEANED) ===\n$fishRaw\n=== END ===\n")
//
//    // Optional: verify the numbers for a specific item like Hurulla
//    val hurullaLine = rawText.lines().find { it.contains("Hurulla", ignoreCase = true) }
//    if (hurullaLine != null) {
//        println("✅ Hurulla line after cleaning:\n$hurullaLine")
//    } else {
//        println("⚠️ Hurulla not found in cleaned text.")
//    }
//}

import java.io.File
import kotlinx.coroutines.runBlocking

fun extractTextFromPdf(pdfBytes: ByteArray): String {
    return Loader.loadPDF(pdfBytes).use { document ->
        PDFTextStripper().apply {
            sortByPosition = true
            // startPage = 2   // Uncomment to skip summary page
        }.getText(document).let { raw ->
            cleanExtractedText(raw)
        }
    }
}

//fun extractTextFromPdf(filepath: String): String {
//    val bytes = File(filepath).readBytes()
//    return extractTextFromPdf(bytes)
//}

private fun cleanExtractedText(raw: String): String {
    // 1. Remove footer lines that are only dates
    val lines = raw.lines()
        .filter { line ->
            !line.matches(Regex("""^\s*(\d{1,2}-[A-Za-z]{3}\s+)+$""")) &&
                    !line.matches(Regex("""^\s*\d{1,2}-[A-Za-z]{3}\s+\d{1,2}-[A-Za-z]{3}""")) &&
                    line.isNotBlank()
        }
        .joinToString("\n")

    // 2. Collapse all whitespace into single spaces
    var cleaned = lines.replace(Regex("\\s+"), " ")

    // 3. Remove spaces inside numbers: "1 ,900" → "1,900", "2 50" → "250"
    cleaned = cleaned.replace(Regex("""(?<=\d)\s+(?=[,.\d])"""), "")

    // 4. FORCE a space between any two numbers that are glued together
    //    e.g., "1,900.001,300.00" → "1,900.00 1,300.00"
    cleaned = cleaned.replace(Regex("""(\d+(?:,\d+)?\.\d{2})(?=\d)"""), "$1 ")

    // 5. Normalise "n.a."
    cleaned = cleaned.replace(Regex("""n\.a\.""", RegexOption.IGNORE_CASE), "n.a.")

    // 6. Final collapse and trim
    cleaned = cleaned.replace(Regex("\\s+"), " ").trim()

    return cleaned
}

//fun main() = runBlocking {
//    val pdfPath = "price_report_20260727_e.pdf"   // Update path
//    val rawText = extractTextFromPdf(pdfPath)
//
//    File("full_raw_text_cleaned.txt").writeText(rawText)
//    println("✅ Cleaned full text written to full_raw_text_cleaned.txt (length: ${rawText.length})")
//
//    val fishStart = rawText.indexOf("FISH", ignoreCase = true)
//    if (fishStart == -1) {
//        println("❌ Could not find 'FISH'. Dumping first 500 chars:")
//        println(rawText.take(500))
//        return@runBlocking
//    }
//
//    val fishEnd = rawText.indexOf("RICE", fishStart, ignoreCase = true)
//    val fishRaw = rawText.substring(fishStart, if (fishEnd != -1) fishEnd else rawText.length)
//
//    println("\n=== 🐟 FISH SECTION ===\n$fishRaw\n=== END ===\n")
//
//    val hurullaLine = rawText.lines().find { it.contains("Hurulla", ignoreCase = true) }
//    if (hurullaLine != null) {
//        println("✅ Hurulla line:\n$hurullaLine")
//    } else {
//        println("⚠️ Hurulla not found.")
//    }
//}