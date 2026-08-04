package com.sangeeth

import java.time.LocalDate

suspend fun processDate(date: LocalDate) {
    println("Fetching report for $date...")
    val pdf = fetchPdfForDate(date)
    if (pdf == null) {
        println("No PDF found for $date")
        return
    }

    val report = parsePriceReport(pdf, date.toString())
    val finalJson = report.toJson()

    saveReportPersistent(date, finalJson)
//    saveReport(date, finalJson)
    cleanupOldReports()

    println("Saved report for $date")
}