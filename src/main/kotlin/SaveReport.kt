package com.sangeeth

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun saveReport(date: LocalDate, json: String){
    val fileName = "price_${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}.json"
    val file = File("data", fileName)
    file.parentFile.mkdirs()
    file.writeText(json)
}