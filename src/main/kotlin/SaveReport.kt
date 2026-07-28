//package com.sangeeth
//
//import java.io.File
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//
//fun saveReport(date: LocalDate, json: String){
//    val fileName = "price_${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}.json"
//    val file = File("data", fileName)
//    file.parentFile.mkdirs()
//    file.writeText(json)
//}

package com.sangeeth

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun saveReport(date: LocalDate, json: String) {
    val dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    val dir = File("data")
    if (!dir.exists()) dir.mkdirs()

    val file = File(dir, "price_report_$dateStr.json")
    file.writeText(json)
    println("Saved: ${file.absolutePath}")
}

fun cleanupOldReports(keepDays: Int = 30) {
    val dir = File("data")
    if (!dir.exists()) return

    val cutoff = LocalDate.now().minusDays(keepDays.toLong())
    val pattern = Regex("""price_report_(\d{8})\.json""")

    dir.listFiles { file ->
        val match = pattern.matchEntire(file.name)
        if (match != null) {
            val fileDate = LocalDate.parse(match.groupValues[1], DateTimeFormatter.ofPattern("yyyyMMdd"))
            fileDate.isBefore(cutoff)
        } else false
    }?.forEach { it.delete() }
}