package com.sangeeth

import java.time.LocalDate

suspend fun processDate(date: LocalDate) {
    println("Fetching report for $date...")
    val pdf = fetchPdfForDate(date)
    if (pdf == null) {
        println("No PDF found for $date")
        return
    }
    val rawText = extractTextFromPdf(pdf)
    val report = extractPriceData(rawText)
    val finalJson = report.toRenamedJson()
    saveReport(date, finalJson)
    cleanupOldReports()
    println("✅ Saved report for $date")
}