package com.sangeeth

import io.ktor.server.cio.CIO
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpStatusCode

suspend fun fetchPdfForDate(date: LocalDate): ByteArray?{
    var dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    val url = "https://www.cbsl.gov.lk/sites/default/files/cbslweb_documents/statistics/pricerpt/price_report_${date}_e.pdf"

    val client = HttpClient()
    try {

        val response = client.get(url)
        return when (response.status){
            HttpStatusCode.OK -> response.bodyAsBytes()
            HttpStatusCode.NotFound -> null
            else -> {
                println("error fetching $dateStr: ${response.status}")
                null
            }
        }
    }catch (e: Exception){
        println("Exception fetching $dateStr: ${e.message}")
        return null
    }finally {

        client.close()
    }
}