package com.sangeeth

import io.ktor.server.cio.CIO
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay

//suspend fun fetchPdfForDate(date: LocalDate, maxRetries: Int = 3): ByteArray?{
//    var dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
//    val url = "https://www.cbsl.gov.lk/sites/default/files/cbslweb_documents/statistics/pricerpt/price_report_${dateStr}_e.pdf"
//
//    var retries = 0
//    while (retries < maxRetries){
//        val client = HttpClient()
//        try {
//
//            val headResponse = client.head(url)
//            when (headResponse.status) {
//                HttpStatusCode.OK -> {
//                    val getResponse = client.get(url)
//                    if (getResponse.status == HttpStatusCode.OK) {
//                        val bytes = getResponse.bodyAsBytes()
//                        if (bytes.size >= 4 &&
//                            bytes[0] == 0x25.toByte() &&
//                            bytes[1] == 0x50.toByte() &&
//                            bytes[2] == 0x44.toByte() &&
//                            bytes[3] == 0x46.toByte()
//                        ) {
//                            return bytes
//                        } else {
//                            println("Download content is not a valid pdf")
//                            return null
//                        }
//                    } else {
//                        println("GET failed ${getResponse.status}")
//                        return null
//                    }
//                }
//
//                HttpStatusCode.NotFound -> {
//                    println("404 for $dateStr (attemt ${retries + 1}/$maxRetries")
//                    retries++
//                    if (retries >= maxRetries) return null
//                    delay(1000L * retries)
//                }
//
//                else -> {
//                    println("HEAD error: ${headResponse.status}")
//                    return null
//                }
//            }
//        }catch (e: Exception){
//            println("Exception: ${e.message}")
//            retries++
//            if (retries>= maxRetries) return null
//            delay(1000L * retries)
//
//        } finally {
//            client.close()
//        }
//    }
//    return null
//
//}