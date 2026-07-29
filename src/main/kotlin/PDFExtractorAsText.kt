package com.sangeeth

import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper

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

