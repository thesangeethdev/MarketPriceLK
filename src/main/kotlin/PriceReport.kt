package com.sangeeth

import kotlinx.serialization.Serializable
import javax.xml.crypto.Data


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
    val wholesale: Market,
    val retail: Market,
    val narahenpita: DayPair
)

@Serializable
data class Market(
    val pettah: DayPair,
    val dambulla: DayPair
)

@Serializable
data class DayPair(
    val yesterday: Double?,
    val today: Double?
)


