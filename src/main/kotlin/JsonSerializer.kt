package com.sangeeth

import kotlinx.serialization.json.Json

private val json = Json {
    prettyPrint = true
    encodeDefaults = false
    explicitNulls = true
}

fun PriceReport.toJson(): String {
    return json.encodeToString(this)
}