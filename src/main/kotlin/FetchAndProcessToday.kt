package com.sangeeth

import toRenamedJson
import java.time.LocalDate

suspend fun fetchAndProcessToday(){
    val today = LocalDate.now()
    println("Fetching report for $today")

    val pdfBytes = fetchPdfForDate(today)
    if (pdfBytes == null){
        println("No PDf for $today")
        return
    }

    val rawText = extractTextFromPdf(pdfBytes)
    val report = extractPriceData(rawText)
    val finalJson = report.toRenamedJson()
    saveReport(today, finalJson)
    cleanupOldReports()
    println("Dont for $today")
}