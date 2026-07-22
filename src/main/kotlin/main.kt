package com.sangeeth

import io.ktor.server.engine.*
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File

fun main() = runBlocking {

    val pdfPath = "price_report_20260722_e.pdf"
    val rawText = extractTextFromPdf(pdfPath)

    val report = extractPriceData(rawText)

    val jsonString = Json.encodeToString(report)
    File("output.json").writeText(jsonString)
//    println("Report date: ${report.reportDate}")
//    println("Summary: ${report.summary ?: "No Summary"} ")
//    report.sections.forEach { section ->
//        println("\n${section.sectionName}")
//        section.items.forEach { item ->
//            val today = item.retail.pettah.today
//            println("   ${item.item}: ${item.unit} | Pettah retail todat: $today")
//        }
//    }
}
