package com.sangeeth

import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.Timer
import kotlin.concurrent.timerTask

object DailySchedular{
    private var timer: Timer? = null

    fun start(){
        val zone = ZoneId.of("Asia/Colombo")
        val now = ZonedDateTime.now(zone)

        var nextRun = now.withHour(9).withMinute(0).withSecond(0)
        if(now>= nextRun){
            nextRun = nextRun.plusDays(1)

        }

        val delay = nextRun.toInstant().toEpochMilli() - System.currentTimeMillis()
        timer = Timer("DailyReportTimer", true)
        timer?.scheduleAtFixedRate(timerTask {
            val today = LocalDate.now(zone)
            println("=== timer triggered at ${Date()} ===")
            runBlocking {
                processDate(today)
            }
        }, delay, 24 * 60 * 60 * 1000)
    }
    fun shutDown(){
        timer?.cancel()
    }
}