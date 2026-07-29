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

fun cleanupOldReports(keepDays: Int = 3) {
    val dataDir = File("data")
    if (!dataDir.exists() || !dataDir.isDirectory) return

    val pattern = Regex("""price_report_(\d{8})\.json""")
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    val filesWithDates = dataDir.listFiles { _, name ->
        name.startsWith("price_report_") && name.endsWith(".json")
    }?.mapNotNull { file ->
        val match = pattern.matchEntire(file.name)
        if (match != null) {
            try {
                val date = LocalDate.parse(match.groupValues[1], formatter)
                file to date
            } catch (_: Exception) {
                null
            }
        } else null
    }?.sortedByDescending { it.second }  // Sort by actual date, newest first
        ?: emptyList()

    if (filesWithDates.size > keepDays) {
        filesWithDates.drop(keepDays).forEach { (file, date) ->
            file.delete()
            println("Deleted old report: ${file.name} ($date)")
        }
    }
}