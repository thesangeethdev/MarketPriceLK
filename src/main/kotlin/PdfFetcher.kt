package com.sangeeth

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

suspend fun fetchPdfForDate(date: LocalDate, maxRetries: Int = 3): ByteArray? {
    val dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    val url = "https://www.cbsl.gov.lk/sites/default/files/cbslweb_documents/statistics/pricerpt/price_report_${dateStr}_e.pdf"

    val client = HttpClient(CIO) {
        expectSuccess = false
    }

    var retries = 0
    while (retries < maxRetries) {
        try {
            val headResponse = client.head(url)
            when (headResponse.status) {
                HttpStatusCode.OK -> {
                    val getResponse = client.get(url)
                    if (getResponse.status == HttpStatusCode.OK) {
                        val bytes = getResponse.bodyAsBytes()
                        if (bytes.size >= 4 &&
                            bytes[0] == 0x25.toByte() &&
                            bytes[1] == 0x50.toByte() &&
                            bytes[2] == 0x44.toByte() &&
                            bytes[3] == 0x46.toByte()
                        ) {
                            client.close()
                            return bytes
                        }
                        println("Downloaded content is not a valid PDF")
                        client.close()
                        return null
                    }
                }
                HttpStatusCode.NotFound -> {
                    println("404 for $dateStr (attempt ${retries + 1}/$maxRetries)")
                    retries++
                    if (retries >= maxRetries) {
                        client.close()
                        return null
                    }
                    delay(1000L * retries)
                }
                else -> {
                    println("HEAD error: ${headResponse.status}")
                    client.close()
                    return null
                }
            }
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            retries++
            if (retries >= maxRetries) {
                client.close()
                return null
            }
            delay(1000L * retries)
        }
    }

    client.close()
    return null
}