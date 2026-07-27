package com.sangeeth

import io.ktor.server.engine.*
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File

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

    fetchAndProcessToday()
}
