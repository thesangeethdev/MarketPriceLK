package com.sangeeth

import io.ktor.server.engine.*
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate

fun main() = runBlocking {

    println("Starting backfill for last 3 days...")
//    processDate(LocalDate.of(2026, 7, 24))
    backFillLast3Days()
    println("Backfill complete. Check the 'data/' folder for the saved JSONs.")

}
