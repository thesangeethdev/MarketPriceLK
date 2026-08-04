package com.sangeeth

import io.ktor.client.engine.cio.CIO
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.*
import io.ktor.server.application.*
import io.ktor.server.http.content.files
import io.ktor.server.netty.EngineMain
import io.ktor.server.response.respondText
import io.ktor.server.routing.contentType
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
fun main(args: Array<String>) {
    DailySchedular.start()
    EngineMain.main(args)
}

fun Application.module() {
    routing {
        get("/") {
            call.respondText("Hello market-price-lk!")
        }
        get("/run-now"){
            val date = LocalDate.now(ZoneId.of("Asia/Colombo"))
            GlobalScope.launch {
                processDate(date)
            }
            call.respondText("report processing started for $date")
        }

        get("/backfill") {
            backFillLast3Days()
            call.respondText("Backfill complete")
        }

        get("/latest"){

            //supabase first
            val report = getLatestReportFromDb()
            if (report != null){
                call.respondText(
                    Json.encodeToString(report.data),
                    contentType = ContentType.Application.Json
                )
                return@get
            }

            val dir = File("data")
            val latestFile = dir.listFiles { f -> f.name.endsWith(".json") }
                ?.maxByOrNull { it.name }
            if (latestFile == null) {
                return@get call.respondText(
                    "No reports available",
                    status = HttpStatusCode.NotFound
                )
            }

            call.respondText(
                latestFile.readText(),
                contentType = ContentType.Application.Json
            )
        }
        get("/reports"){

            //supabase first
            val dbReports = getAllReportsFromDb()
            if (dbReports.isNotEmpty()){
                val names = dbReports.map { "price_report_${it.date}.json" }
                call.respondText(names.joinToString("\n"))
                return@get
            }


            val dir = File("data")
            val files = if (dir.exists()){
                dir.listFiles{file -> file.name.endsWith(".json")}?.map {
                    it.name
                } ?: emptyList()
            } else emptyList()
            call.respondText(files.joinToString("\n"))
        }

        get("/reports/{date}"){
            val date = call.parameters["date"] ?: return@get
            if (date == null){
                call.respondText(
                    "Missing date. Format YYYYMMDD",
                    status = HttpStatusCode.BadRequest
                )
            }

            // supabase first
            val report = getReportFromDb(date)
            if (report != null){
                call.respondText(
                    Json.encodeToString(report.data),
                    contentType = ContentType.Application.Json
                )
                return@get
            }


            val file = File("data/price_report_$date.json")
            if (!file.exists()){
                return@get call.respondText(
                    "Report not found for $date, Please use /reports endpoint to see available dates",
                    status = HttpStatusCode.NotFound
                )
            }
            call.respondText(
                file.readText(),
                contentType = ContentType.Application.Json
            )
        }

        get("/history") {

            // supabase first
            val dbreports = getAllReportsFromDb().take(3)
            if (dbreports.isNotEmpty()){
                val combined = dbreports.map { report ->
                    """{"date":"${report.date}","data":${Json.encodeToString(report.data)}}"""
                }
                call.respondText(
                    "[" + combined.joinToString(",") + "]",
                    contentType = ContentType.Application.Json
                )
                return@get
            }


            val dir = File("data")
            val reports = dir.listFiles { f -> f.name.endsWith(".json") }
                ?.sortedByDescending { it.name }
                ?.take(3)
                ?.map { file ->
                    val date = file.name.removePrefix("price_report_").removeSuffix(".json")
                    val rawJson = file.readText()
                    """{"date":"$date","data":$rawJson}"""
                } ?: emptyList()

            val combined = "[" + reports.joinToString(",") + "]"
            call.respondText(
                combined,
                contentType = ContentType.Application.Json
            )
        }
    }
}
