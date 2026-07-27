package com.sangeeth

import java.time.LocalDate

suspend fun backFillLast3Days(){
    var count = 0
    var date = LocalDate.now()
    while (count<3 && date.isAfter(LocalDate.of(2020,1,1))){
        val pdf = fetchPdfForDate(date)
        if (pdf!= null){
            val rawText = extractTextFromPdf(pdf)
            val report = extractPriceData(rawText)
            val finalJson = report.toRenamedJson()
            saveReport(date, finalJson)
            count++
        }
        date = date.minusDays(1)
    }
    cleanupOldReports()
}