package com.sangeeth

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

suspend fun backFillLast3Days() {
    val zone = ZoneId.of("Asia/Colombo")
    var count = 0
    var date = LocalDate.now(zone)
    val maxAttempts = 10  // Search up to 10 days back

    var attempts = 0
    while (count < 3 && attempts < maxAttempts) {
        println("Backfill attempt ${attempts + 1}: checking $date")
        val pdf = fetchPdfForDate(date)
        if (pdf != null) {
            println("Found PDF for $date")
            val report = parsePriceReport(pdf, date.toString())
            val finalJson = report.toJson()
//            saveReport(date, finalJson)
//            saveReportToDb(
//                date,finalJson
//            )
            saveReportPersistent(date, finalJson)
            count++
        } else {
            println("No PDF for $date")
        }
        date = date.minusDays(1)
        attempts++
    }

    cleanupOldReports()
    println("Backfill complete. Found $count reports.")
}