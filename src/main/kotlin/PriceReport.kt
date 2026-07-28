package com.sangeeth

import kotlinx.serialization.Serializable

@Serializable
data class PriceReport(
    val reportDate: String,
    val summary: String?,
    val sections: List<Section>
)

@Serializable
data class Section(
    val sectionName: String,
    val items: List<Item>
)

@Serializable
data class Item(
    val item: String,
    val unit: String,
    val wholesale: Map<String, DayPair?>,
    val retail: Map<String, DayPair?>
)

@Serializable
data class DayPair(
    val yesterday: Double?,
    val today: Double?
)