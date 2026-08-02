package com.sangeeth

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val pdfClient = HttpClient(CIO) {
    expectSuccess = false
    engine {
        requestTimeout = 60000  // 60 seconds
        endpoint {
            connectTimeout = 30000  // 30 seconds
        }
    }
}

suspend fun fetchPdfForDate(date: LocalDate, maxRetries: Int = 5): ByteArray? {
    val dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    val url = "https://www.cbsl.gov.lk/sites/default/files/cbslweb_documents/statistics/pricerpt/price_report_${dateStr}_e.pdf"

    var retries = 0
    while (retries < maxRetries) {
        try {
            println("Checking $url (attempt ${retries + 1}/$maxRetries)")

            val headResponse = pdfClient.head(url)
            when (headResponse.status) {
                HttpStatusCode.OK -> {
                    println("HEAD OK for $dateStr, downloading...")
                    val getResponse = pdfClient.get(url)
                    if (getResponse.status == HttpStatusCode.OK) {
                        val bytes = getResponse.bodyAsBytes()
                        if (isValidPdf(bytes)) {
                            println("Valid PDF downloaded for $dateStr (${bytes.size} bytes)")
                            return bytes
                        }
                        println("Downloaded content is not a valid PDF")
                        return null
                    }
                }
                HttpStatusCode.NotFound -> {
                    println("404 for $dateStr (attempt ${retries + 1}/$maxRetries)")
                }
                else -> {
                    println("HEAD error for $dateStr: ${headResponse.status}")
                }
            }
        } catch (e: Exception) {
            println("Exception fetching $dateStr: ${e.message}")
        }

        retries++
        if (retries < maxRetries) {
            val delayMs = 3000L * retries  // 3s, 6s, 9s, 12s, 15s
            println("Retrying in ${delayMs}ms...")
            delay(delayMs)
        }
    }

    println("Failed to fetch PDF for $dateStr after $maxRetries attempts")
    return null
}

private fun isValidPdf(bytes: ByteArray): Boolean {
    return bytes.size >= 4 &&
            bytes[0] == 0x25.toByte() &&
            bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x44.toByte() &&
            bytes[3] == 0x46.toByte()
}