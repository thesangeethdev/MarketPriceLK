//package com.sangeeth

//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.JsonArray
//import kotlinx.serialization.json.JsonNull
//import kotlinx.serialization.json.JsonObject
//import kotlinx.serialization.json.encodeToJsonElement
//import kotlinx.serialization.json.jsonArray
//import kotlinx.serialization.json.jsonObject
//import kotlinx.serialization.json.jsonPrimitive
//import kotlin.collections.forEach
//import kotlin.collections.mapOf
//import kotlinx.serialization.json.Json
//import com.sangeeth.PriceReport
//import kotlinx.serialization.json.encodeToJsonElement
//import kotlinx.serialization.json.jsonArray
//import kotlinx.serialization.json.jsonObject
//import kotlinx.serialization.json.jsonPrimitive
//import kotlin.collections.forEach
//import kotlin.collections.linkedMapOf
//import kotlin.collections.mapOf
////private val prettyJson = Json { prettyPrint = true }
//
////fun PriceReport.toRenamedJson(): String {
////    val root = Json.encodeToJsonElement(this).jsonObject.toMutableMap()
////
////    val newSections = root["sections"]!!.jsonArray.map { element ->
////        val sectionObj = element.jsonObject
////        val sectionName = sectionObj["sectionName"]!!.jsonPrimitive.content
////
////        val (wholesaleMap, retailMap) = when (sectionName) {
////
////            "Vegetables", "Other", "Fruits" ->
////                mapOf("pettah" to "pettah", "dambulla" to "dambulla") to
////                        mapOf("pettah" to "pettah", "dambulla" to "dambulla")
////
////            "Rice" ->
////                mapOf("pettah" to "pettah", "dambulla" to "marandagahamula")  to
////                        mapOf("pettah" to "pettah", "dambulla" to "dambulla")
////
////            "Fish" ->
////                mapOf("pettah" to "peliyagoda", "dambulla" to "negombo") to
////                        mapOf("pettah" to "pettah", "dambulla" to "negombo")
////            else ->
////                mapOf("pettah" to "pettah", "dambulla" to "dambulla") to
////                        mapOf("pettah" to "pettah", "dambulla" to "dambulla")
////        }
////
////        val newItems = sectionObj["items"]!!.jsonArray.map { element ->
////            val itemObj = element.jsonObject.toMutableMap()
////
////            val narahenpitaObj = itemObj.remove("narahenpita")
////
////            val wholesaleObj = itemObj["wholesale"]!!.jsonObject.toMutableMap()
////            wholesaleMap.forEach { oldKey, newKey ->
////                if (oldKey != newKey){
////                    wholesaleObj[newKey] = wholesaleObj.remove(oldKey)!!
////                }
////            }
////            itemObj["wholesale"] = JsonObject(wholesaleObj)
////
////            val retailObj = itemObj["retail"]!!.jsonObject.toMutableMap()
////            retailMap.forEach { oldKey, newKey ->
////                if (oldKey != newKey){
////                    retailObj[newKey] = retailObj.remove(oldKey)!!
////                }
////            }
////
////            if (narahenpitaObj != null){
////                retailObj["narahenpita"] = narahenpitaObj
////            }
////
////            val finalRetailObj = if (sectionName.equals("Fish", ignoreCase = true)) {
////                // desired order: negombo, narahenpita, pettah
////                val orderedMap = linkedMapOf(
////                    "negombo" to retailObj["negombo"],
////                    "narahenpita" to retailObj["narahenpita"],
////                    "pettah" to retailObj["pettah"]
////                ).filterValues { it != null }.mapValues { (_, v) -> v!! }
////                JsonObject(orderedMap)
////            } else {
////                JsonObject(retailObj)
////            }
////
////            itemObj["retail"] = finalRetailObj
////            JsonObject(itemObj)
////        }
////        val newSection = sectionObj.toMutableMap()
////        newSection["items"] = JsonArray(newItems)
////        JsonObject(newSection)
////    }
////    root["sections"] = JsonArray(newSections)
////    val finalJson = JsonObject(root)
////    return prettyJson.encodeToString(finalJson)
////}
//
////fun PriceReport.toRenamedJson(): String {
////    val root = Json.encodeToJsonElement(this).jsonObject.toMutableMap()
////
////    val newSections = root["sections"]!!.jsonArray.map { element ->
////        val sectionObj = element.jsonObject
////        val sectionName = sectionObj["sectionName"]!!.jsonPrimitive.content
////
////        val (wholesaleMap, retailMap) = when (sectionName) {
////            "Vegetables", "Other", "Fruits" ->
////                mapOf("pettah" to "pettah", "dambulla" to "dambulla") to
////                        mapOf("pettah" to "pettah", "dambulla" to "dambulla")
////
////            "Rice" ->
////                mapOf("pettah" to "pettah", "dambulla" to "marandagahamula") to
////                        mapOf("pettah" to "pettah", "dambulla" to "dambulla")
////
////            "Fish" ->
////                mapOf("pettah" to "peliyagoda", "dambulla" to "negombo") to
////                        mapOf("pettah" to "pettah", "dambulla" to "negombo")
////
////            else ->
////                mapOf("pettah" to "pettah", "dambulla" to "dambulla") to
////                        mapOf("pettah" to "pettah", "dambulla" to "dambulla")
////        }
////
////        val newItems = sectionObj["items"]!!.jsonArray.map { itemElement ->
////            val itemObj = itemElement.jsonObject.toMutableMap()
////
////            // For Fish, there might be a top‑level "narahenpita" field – we store it.
////            val narahenpitaObj = if (sectionName == "Fish") {
////                itemObj.remove("narahenpita")
////            } else null
////
////            // --- Rename wholesale keys ---
////            val wholesaleObj = itemObj["wholesale"]!!.jsonObject.toMutableMap()
////            wholesaleMap.forEach { oldKey, newKey ->
////                if (oldKey != newKey) {
////                    wholesaleObj[newKey] = wholesaleObj.remove(oldKey)!!
////                }
////            }
////            itemObj["wholesale"] = JsonObject(wholesaleObj)
////
////            // --- Rename retail keys according to retailMap ---
////            val retailObj = itemObj["retail"]!!.jsonObject.toMutableMap()
////            retailMap.forEach { oldKey, newKey ->
////                if (oldKey != newKey) {
////                    retailObj[newKey] = retailObj.remove(oldKey)!!
////                }
////            }
////
////            // If we extracted a top‑level narahenpita object, add it to retailObj
////            if (narahenpitaObj != null) {
////                retailObj["narahenpita"] = narahenpitaObj
////            }
////
////            // --- Fish‑specific: swap narahenpita and pettah ---
////            val finalRetailObj = if (sectionName == "Fish") {
////                // Swap the two objects
////                val narahenpitaValue = retailObj["narahenpita"] ?: JsonNull
////                val pettahValue = retailObj["pettah"] ?: JsonNull
////                retailObj["pettah"] = narahenpitaValue   // old narahenpita → pettah
////                retailObj["narahenpita"] = pettahValue   // old pettah → narahenpita
////
////                // Build ordered map: negombo, pettah, narahenpita
////                val orderedMap = linkedMapOf(
////                    "negombo" to retailObj["negombo"],
////                    "pettah" to retailObj["pettah"],
////                    "narahenpita" to retailObj["narahenpita"]
////                ).mapValues { (_, v) -> v ?: JsonNull } // keep nulls explicitly
////                JsonObject(orderedMap)
////            } else {
////                // For other sections, keep the renamed retail as is (preserve nulls)
////                JsonObject(retailObj)
////            }
////
////            itemObj["retail"] = finalRetailObj
////            JsonObject(itemObj)
////        }
////
////        val newSection = sectionObj.toMutableMap()
////        newSection["items"] = JsonArray(newItems)
////        JsonObject(newSection)
////    }
////
////    root["sections"] = JsonArray(newSections)
////    val finalJson = JsonObject(root)
////    return prettyJson.encodeToString(finalJson)
////}
//
//
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.JsonArray
//import kotlinx.serialization.json.JsonNull
//import kotlinx.serialization.json.JsonObject
//import kotlinx.serialization.json.encodeToJsonElement
//import kotlinx.serialization.json.jsonArray
//import kotlinx.serialization.json.jsonObject
//import kotlinx.serialization.json.jsonPrimitive
//import kotlin.collections.forEach
//import kotlin.collections.linkedMapOf
//import kotlin.collections.mapOf
//
//private val prettyJson = Json { prettyPrint = true }
//
//fun PriceReport.toRenamedJson(): String {
//    val root = Json.encodeToJsonElement(this).jsonObject.toMutableMap()
//
//    val newSections = root["sections"]!!.jsonArray.map { sectionElement ->
//        val sectionObj = sectionElement.jsonObject
//        val sectionName = sectionObj["sectionName"]!!.jsonPrimitive.content
//
//        // Define renaming maps for each section
//        val (wholesaleMap, retailMap) = when (sectionName) {
//            "Vegetables", "Other", "Fruits" ->
//                mapOf("pettah" to "pettah", "dambulla" to "dambulla") to
//                        mapOf("pettah" to "pettah", "dambulla" to "dambulla")
//
//            "Rice" ->
//                mapOf("pettah" to "pettah", "dambulla" to "marandagahamula") to
//                        mapOf("pettah" to "pettah", "dambulla" to "dambulla")
//
//            "Fish" ->
//                mapOf("pettah" to "peliyagoda", "dambulla" to "negombo") to
//                        mapOf("pettah" to "pettah", "dambulla" to "negombo")
//
//            else ->
//                mapOf("pettah" to "pettah", "dambulla" to "dambulla") to
//                        mapOf("pettah" to "pettah", "dambulla" to "dambulla")
//        }
//
//        val newItems = sectionObj["items"]!!.jsonArray.map { itemElement ->
//            val itemObj = itemElement.jsonObject.toMutableMap()
//
//            // For Fish, there might be a top‑level "narahenpita" field (old format).
//            val narahenpitaObj = if (sectionName == "Fish") {
//                itemObj.remove("narahenpita")
//            } else null
//
//            // --- Rename wholesale keys (safe: only moves if old key exists) ---
//            val wholesaleObj = itemObj["wholesale"]!!.jsonObject.toMutableMap()
//            wholesaleMap.forEach { oldKey, newKey ->
//                if (oldKey != newKey) {
//                    wholesaleObj.remove(oldKey)?.let { value ->
//                        wholesaleObj[newKey] = value
//                    }
//                }
//            }
//            itemObj["wholesale"] = JsonObject(wholesaleObj)
//
//            // --- Rename retail keys (safe) ---
//            val retailObj = itemObj["retail"]!!.jsonObject.toMutableMap()
//            retailMap.forEach { oldKey, newKey ->
//                if (oldKey != newKey) {
//                    retailObj.remove(oldKey)?.let { value ->
//                        retailObj[newKey] = value
//                    }
//                }
//            }
//
//            // If we extracted a top‑level narahenpita object (old format), add it to retailObj.
//            if (narahenpitaObj != null) {
//                retailObj["narahenpita"] = narahenpitaObj
//            }
//
//            // --- Section‑specific handling ---
//            val finalRetailObj = if (sectionName == "Fish") {
//                // For Fish, force "pettah" to always be null and enforce order
//                retailObj["pettah"] = JsonNull
//
//                val orderedMap = linkedMapOf(
//                    "negombo" to retailObj["negombo"],
//                    "narahenpita" to retailObj["narahenpita"],
//                    "pettah" to retailObj["pettah"]
//                ).mapValues { (_, v) -> v ?: JsonNull } // keep nulls explicitly
//                JsonObject(orderedMap)
//            } else {
//                // For other sections, keep the renamed retail as is
//                JsonObject(retailObj)
//            }
//
//            itemObj["retail"] = finalRetailObj
//            JsonObject(itemObj)
//        }
//
//        val newSection = sectionObj.toMutableMap()
//        newSection["items"] = JsonArray(newItems)
//        JsonObject(newSection)
//    }
//
//    root["sections"] = JsonArray(newSections)
//    val finalJson = JsonObject(root)
//    return prettyJson.encodeToString(finalJson)
//}


import com.sangeeth.PriceReport
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.collections.forEach
import kotlin.collections.linkedMapOf
import kotlin.collections.mapOf

val prettyJson = Json { prettyPrint = true }

fun PriceReport.toRenamedJson(): String {
    val root = Json.encodeToJsonElement(this).jsonObject.toMutableMap()

    val newSections = root["sections"]!!.jsonArray.map { sectionElement ->
        val sectionObj = sectionElement.jsonObject
        val sectionName = sectionObj["sectionName"]!!.jsonPrimitive.content

        // Define renaming maps for each section
        val (wholesaleMap, retailMap) = when (sectionName) {
            "Vegetables", "Other", "Fruits" ->
                mapOf("pettah" to "pettah", "dambulla" to "dambulla") to
                        mapOf("pettah" to "pettah", "dambulla" to "dambulla")

            "Rice" ->
                mapOf("pettah" to "pettah", "dambulla" to "marandagahamula") to
                        mapOf("pettah" to "pettah", "dambulla" to "dambulla")

            "Fish" ->
                mapOf("pettah" to "peliyagoda", "dambulla" to "negombo") to
                        mapOf("pettah" to "pettah", "dambulla" to "negombo")

            else ->
                mapOf("pettah" to "pettah", "dambulla" to "dambulla") to
                        mapOf("pettah" to "pettah", "dambulla" to "dambulla")
        }

        val newItems = sectionObj["items"]!!.jsonArray.map { itemElement ->
            val itemObj = itemElement.jsonObject.toMutableMap()

            // For Fish, there might be a top‑level "narahenpita" field (old format).
            val narahenpitaObj = if (sectionName == "Fish") {
                itemObj.remove("narahenpita")
            } else null

            // --- Rename wholesale keys (safe: only moves if old key exists) ---
            val wholesaleObj = itemObj["wholesale"]!!.jsonObject.toMutableMap()
            wholesaleMap.forEach { oldKey, newKey ->
                if (oldKey != newKey) {
                    wholesaleObj.remove(oldKey)?.let { value ->
                        wholesaleObj[newKey] = value
                    }
                }
            }
            itemObj["wholesale"] = JsonObject(wholesaleObj)

            // --- Rename retail keys (safe) ---
            val retailObj = itemObj["retail"]!!.jsonObject.toMutableMap()
            retailMap.forEach { oldKey, newKey ->
                if (oldKey != newKey) {
                    retailObj.remove(oldKey)?.let { value ->
                        retailObj[newKey] = value
                    }
                }
            }

            // If we extracted a top‑level narahenpita object (old format), add it to retailObj.
            if (narahenpitaObj != null) {
                retailObj["narahenpita"] = narahenpitaObj
            }

            // --- Section‑specific handling with post‑processing correction for Fish ---
            val finalRetailObj = if (sectionName == "Fish") {
                // -------- POST‑PROCESSING CORRECTION (for AI mistakes) --------
                // Common issue: AI puts the 7‑8 pair (should be narahenpita) into negombo,
                // and sets narahenpita = null. We correct that here.
                val negombo = retailObj["negombo"]
                val nara = retailObj["narahenpita"]

                if ((nara == null || nara == JsonNull) && negombo != null && negombo != JsonNull) {
                    // Move the (wrong) negombo values to narahenpita
                    retailObj["narahenpita"] = negombo
                    // We lose the correct 5‑6 pair; set negombo to null to avoid wrong data.
                    retailObj["negombo"] = JsonNull
                }

                // Optional: if both are present, we could check if they are swapped.
                // For example, if negombo values are greater than narahenpita (heuristic).
                // Uncomment the block below if you want to try swapping based on value comparison.
                /*
                else if (negombo != null && negombo != JsonNull && nara != null && nara != JsonNull) {
                    // Try to detect a swap by comparing the "yesterday" values.
                    // This is fragile – only enable if you see consistent swapped outputs.
                    val negYest = (negombo as? JsonObject)?.get("yesterday") as? JsonPrimitive
                    val narYest = (nara as? JsonObject)?.get("yesterday") as? JsonPrimitive
                    if (negYest != null && narYest != null) {
                        val negVal = negYest.content.toDoubleOrNull()
                        val narVal = narYest.content.toDoubleOrNull()
                        if (negVal != null && narVal != null && negVal > narVal) {
                            // Swap them
                            retailObj["negombo"] = nara
                            retailObj["narahenpita"] = negombo
                        }
                    }
                }
                */

                // Force pettah to null and enforce order
                retailObj["pettah"] = JsonNull
                val orderedMap = linkedMapOf(
                    "negombo" to retailObj["negombo"],
                    "narahenpita" to retailObj["narahenpita"],
                    "pettah" to retailObj["pettah"]
                ).mapValues { (_, v) -> v ?: JsonNull } // keep nulls explicitly
                JsonObject(orderedMap)
            } else {
                // For other sections, keep the renamed retail as is
                JsonObject(retailObj)
            }

            itemObj["retail"] = finalRetailObj
            JsonObject(itemObj)
        }

        val newSection = sectionObj.toMutableMap()
        newSection["items"] = JsonArray(newItems)
        JsonObject(newSection)
    }

    root["sections"] = JsonArray(newSections)
    val finalJson = JsonObject(root)
    return prettyJson.encodeToString(finalJson)
}