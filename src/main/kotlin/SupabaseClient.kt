package com.sangeeth

import ai.koog.serialization.kotlinx.KotlinxSerializer
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

val supabase: SupabaseClient by lazy {
    createSupabaseClient(
        supabaseUrl = System.getenv("SUPABASE_URL") ?: error("SUPABASE_URL not set"),
        supabaseKey = System.getenv("SUPABASE_KEY") ?: error("SUPABASE_KEY not set")
    ) {
        install(Postgrest)
    }
}

@Serializable
data class SupabasePriceReport(
    val date: String,
    val data: JsonElement
)


suspend fun saveReportToDb(date: String, jsonData: String) {
    val jsonElement = Json.parseToJsonElement(jsonData)
    supabase.from("price_reports")
        .insert(
            SupabasePriceReport(date = date, data = jsonElement),
        )
}

suspend fun getLatestReportFromDb(): SupabasePriceReport? {
    return supabase.from("price_reports")
        .select {
            order(
                "date",
                Order.DESCENDING
            )
            limit(1)
        }
        .decodeSingleOrNull()
}

suspend fun getReportFromDb(date: String): SupabasePriceReport?{
    return supabase.from("price_reports")
        .select {
            filter {
                eq("date", date)
            }
        }
        .decodeSingleOrNull()
}

suspend fun getAllReportsFromDb(): List<SupabasePriceReport>{
    return supabase.from("price_reports")
        .select {
            order("date", Order.DESCENDING)
            limit(10)
        }
        .decodeList()
}