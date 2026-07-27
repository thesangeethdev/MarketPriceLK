package com.sangeeth

import io.ktor.server.engine.*
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate

fun main() = runBlocking {

//    val pdfPath = "price_report_20260722_e.pdf"
//    val rawText = extractTextFromPdf(pdfPath)
////    println(rawText)
//
//    val report = extractPriceData(rawText)
//
//    val finalJson = report.toRenamedJson()
//
//    File("output.json").writeText(finalJson)

//    val pdfPath = "price_report_20260722_e.pdf"   // or any other date
//    val rawText = extractTextFromPdf(pdfPath)   // use the String overload (file path)
//
//    // Find the Fish section (case-insensitive)
//    val fishStart = rawText.indexOf("FISH", ignoreCase = true)
//    if (fishStart == -1) {
//        println("Could not find 'FISH' in the extracted text. Dumping first 500 chars:")
//        println(rawText.take(500))
//        return@runBlocking
//    }
//
//    val fishEnd = rawText.indexOf("RICE", fishStart, ignoreCase = true)
//    val fishRaw = rawText.substring(fishStart, if (fishEnd != -1) fishEnd else rawText.length)
//    File("full_raw_text.txt").writeText(rawText)
//    println("Full raw text written to full_raw_text.txt (length: ${rawText.length})")
//    println("=== FISH RAW TEXT ===\n$fishRaw\n=== END ===")

//    fetchAndProcessToday()
    println("Starting backfill for last 3 days...")
    processDate(LocalDate.of(2026, 7, 21))
    println("Backfill complete. Check the 'data/' folder for the saved JSONs.")
}
