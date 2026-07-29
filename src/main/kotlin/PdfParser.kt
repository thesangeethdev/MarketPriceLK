package com.sangeeth

import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition

class WordExtractor : PDFTextStripper() {
    val words = mutableListOf<Word>()

    init {
        sortByPosition = false
    }

    data class Word(val text: String, val x: Float, val y: Float)

    override fun writeString(text: String, textPositions: List<TextPosition>) {
        val x = textPositions.firstOrNull()?.x ?: 0f
        val y = textPositions.firstOrNull()?.y ?: 0f
        words.add(Word(text.trim(), x, y))
    }

    fun getLines(): List<String> {
        val yTolerance = 3f
        val rows = mutableListOf<MutableList<Word>>()

        for (word in words) {
            val matchedRow = rows.find { row ->
                row.isNotEmpty() && kotlin.math.abs(row.first().y - word.y) < yTolerance
            }
            if (matchedRow != null) {
                matchedRow.add(word)
            } else {
                rows.add(mutableListOf(word))
            }
        }

        return rows.map { row ->
            row.sortedBy { it.x }.joinToString(" ") { it.text }
        }
    }
}

fun extractTableText(pdfBytes: ByteArray): List<String> {
    val doc = Loader.loadPDF(pdfBytes)
    try {
        val extractor = WordExtractor()
        extractor.startPage = 2
        extractor.endPage = 2
        extractor.getText(doc)
        return extractor.getLines()
    } finally {
        doc.close()
    }
}

private val VEGETABLE_ITEMS = setOf(
    "Beans", "Carrot", "Cabbage", "Tomato", "Brinjal",
    "Pumpkin", "Snake gourd", "Green Chilli", "Lime"
)

private val OTHER_ITEMS = setOf(
    "Red Onion (Local)", "Red Onion (lmp)", "Big Onion (Local)", "Big Onion (Imp)",
    "Potato (Local)", "Potato (Imp)", "Dried Chilli (Imp)", "Coconut (Avg.)",
    "Coconut oil", "Red Dhal", "Sugar (White)", "Egg (White)",
    "Katta (Imp)", "Sprat (Imp)"
)

private val FRUIT_ITEMS = setOf(
    "Banana (Sour)", "Papaw", "Pineapple", "Apple (Imp)", "Orange (Imp)"
)

private val RICE_ITEMS = setOf(
    "Samba", "Nadu", "Kekulu (White)", "Kekulu (Red)",
    "Ponni Samba (Imp)", "Nadu (Imp)", "Kekulu (White) (Imp)"
)

private val FISH_ITEMS = setOf(
    "Kelawalla", "Thalapath", "Balaya", "Paraw", "Salaya", "Hurulla", "Linna"
)

private val SPECIAL_OTHER_ITEMS = setOf(
    "Coconut oil", "Red Dhal", "Sugar (White)",
    "Egg (White)", "Katta (Imp)", "Sprat (Imp)"
)
private val SPECIAL_FRUIT_ITEMS = setOf("Apple (Imp)", "Orange (Imp)")
private val SPECIAL_RICE_ITEMS = setOf(
    "Ponni Samba (Imp)", "Nadu (Imp)", "Kekulu (White) (Imp)"
)

fun parsePriceReport(pdfBytes: ByteArray, reportDate: String): PriceReport {
    val lines = extractTableText(pdfBytes)

    val allItems = mutableMapOf<String, Item>()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue
        if (!trimmed.contains("Rs./")) continue
        if (trimmed.contains("Price is not reported")) continue
        if (trimmed.contains("Price increased")) continue
        if (trimmed.contains("Price decreased")) continue

        val item = parseRow(trimmed)
        if (item != null) {
            allItems[item.item] = item
        }
    }

    val vegItems = VEGETABLE_ITEMS.mapNotNull { allItems[it] }
    val otherItems = OTHER_ITEMS.mapNotNull { allItems[it] }
    val fruitItems = FRUIT_ITEMS.mapNotNull { allItems[it] }
    val riceItems = RICE_ITEMS.mapNotNull { allItems[it] }
    val fishItems = FISH_ITEMS.mapNotNull { allItems[it] }

    return PriceReport(
        reportDate = reportDate,
        summary = null,
        sections = listOf(
            Section("Vegetables", vegItems),
            Section("Other", otherItems),
            Section("Fruits", fruitItems),
            Section("Rice", riceItems),
            Section("Fish", fishItems)
        )
    )
}

private fun parseRow(line: String): Item? {
    val unitMatch = Regex("""(Rs\./kg|Rs\./Each|Rs\./Nut|Rs\./Ltr)""").find(line)
        ?: return null

    val itemName = line.substring(0, unitMatch.range.first).trim()
    val unit = unitMatch.value

    val afterUnit = line.substring(unitMatch.range.last + 1).trim()
    val tokens = afterUnit.split(Regex("""\s+"""))

    val values = mutableListOf<Double?>()
    for (token in tokens) {
        val t = token.trim()
        if (t.isEmpty()) continue

        when {
            t.matches(Regex("""n\.?a\.?""", RegexOption.IGNORE_CASE)) -> values.add(null)
            t.matches(Regex("""[\d,]+\.\d{2}""")) -> values.add(t.replace(",", "").toDoubleOrNull())
        }
    }

    return buildItem(itemName, unit, values)
}

private fun buildItem(name: String, unit: String, values: List<Double?>): Item? {
    return when {
        name in VEGETABLE_ITEMS -> buildVegItem(name, unit, values)
        name in OTHER_ITEMS -> buildOtherItem(name, unit, values)
        name in FRUIT_ITEMS -> buildFruitItem(name, unit, values)
        name in RICE_ITEMS -> buildRiceItem(name, unit, values)
        name in FISH_ITEMS -> buildFishItem(name, unit, values)
        else -> {
            println("WARN: Unknown item '$name'")
            null
        }
    }
}

private fun buildVegItem(name: String, unit: String, values: List<Double?>): Item {
    val v = values.pad(10)
    return Item(
        item = name,
        unit = unit,
        wholesale = mapOf(
            "pettah" to DayPair(v[0], v[1]),
            "dambulla" to DayPair(v[2], v[3])
        ),
        retail = mapOf(
            "pettah" to DayPair(v[4], v[5]),
            "dambulla" to DayPair(v[6], v[7]),
            "narahenpita" to DayPair(v[8], v[9])
        )
    )
}

private fun buildOtherItem(name: String, unit: String, values: List<Double?>): Item {
    return if (name in SPECIAL_OTHER_ITEMS) {
        val v = values.pad(6)
        Item(
            item = name,
            unit = unit,
            wholesale = mapOf(
                "pettah" to DayPair(v[0], v[1]),
                "dambulla" to null
            ),
            retail = mapOf(
                "pettah" to DayPair(v[2], v[3]),
                "dambulla" to null,
                "narahenpita" to DayPair(v[4], v[5])
            )
        )
    } else {
        buildVegItem(name, unit, values)
    }
}

private fun buildFruitItem(name: String, unit: String, values: List<Double?>): Item {
    return if (name in SPECIAL_FRUIT_ITEMS) {
        val v = values.pad(4)
        Item(
            item = name,
            unit = unit,
            wholesale = mapOf(
                "pettah" to null,
                "dambulla" to null
            ),
            retail = mapOf(
                "pettah" to DayPair(v[0], v[1]),
                "dambulla" to null,
                "narahenpita" to DayPair(v[2], v[3])
            )
        )
    } else {
        buildVegItem(name, unit, values)
    }
}

private fun buildRiceItem(name: String, unit: String, values: List<Double?>): Item {
    return if (name in SPECIAL_RICE_ITEMS) {
        // Imported rice: 8 values with n.a. for marandagahamula
        // Layout: pettah_w(2) + marandagahamula(2, n.a.) + pettah_r(2) + narahenpita(2)
        // retail.dambulla is completely absent (not even n.a.)
        val v = values.pad(8)
        Item(
            item = name,
            unit = unit,
            wholesale = mapOf(
                "pettah" to DayPair(v[0], v[1]),
                "marandagahamula" to DayPair(v[2], v[3])  // Will be null from n.a.
            ),
            retail = mapOf(
                "pettah" to DayPair(v[4], v[5]),
                "dambulla" to null,  // Always null for imported rice
                "narahenpita" to DayPair(v[6], v[7])
            )
        )
    } else {
        val v = values.pad(10)
        Item(
            item = name,
            unit = unit,
            wholesale = mapOf(
                "pettah" to DayPair(v[0], v[1]),
                "marandagahamula" to DayPair(v[2], v[3])
            ),
            retail = mapOf(
                "pettah" to DayPair(v[4], v[5]),
                "dambulla" to DayPair(v[6], v[7]),
                "narahenpita" to DayPair(v[8], v[9])
            )
        )
    }
}

private fun buildFishItem(name: String, unit: String, values: List<Double?>): Item {
    val v = values.take(8).pad(8)
    return Item(
        item = name,
        unit = unit,
        wholesale = mapOf(
            "peliyagoda" to DayPair(v[0], v[1]),
            "negombo" to DayPair(v[2], v[3])
        ),
        retail = mapOf(
            "negombo" to DayPair(v[4], v[5]),
            "narahenpita" to DayPair(v[6], v[7])
        )
    )
}

private fun List<Double?>.pad(size: Int): List<Double?> {
    return if (this.size >= size) this.take(size) else this + List(size - this.size) { null }
}