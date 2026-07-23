package com.sangeeth

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.collections.forEach
import kotlin.collections.mapOf

private val prettyJson = Json { prettyPrint = true }

fun PriceReport.toRenamedJson(): String {
    val root = Json.encodeToJsonElement(this).jsonObject.toMutableMap()

    val newSections = root["sections"]!!.jsonArray.map { element ->
        val sectionObj = element.jsonObject
        val sectionName = sectionObj["sectionName"]!!.jsonPrimitive.content

        val (wholesaleMap, retailMap) = when (sectionName) {

            "Vegetables", "Other", "Fruits" ->
                mapOf("pettah" to "pettah", "dambulla" to "dambulla") to
                        mapOf("pettah" to "pettah", "dambulla" to "dambulla")

            "Rice" ->
                mapOf("pettah" to "pettah", "dambulla" to "marandagahamula")  to
                        mapOf("pettah" to "pettah", "dambulla" to "dambulla")

            "Fish" ->
                mapOf("pettah" to "peliyagoda", "dambulla" to "negombo") to
                        mapOf("pettah" to "pettah", "dambulla" to "negombo")
            else ->
                mapOf("pettah" to "pettah", "dambulla" to "dambulla") to
                        mapOf("pettah" to "pettah", "dambulla" to "dambulla")
        }

        val newItems = sectionObj["items"]!!.jsonArray.map { element ->
            val itemObj = element.jsonObject.toMutableMap()

            val narahenpitaObj = itemObj.remove("narahenpita")
            
            val wholesaleObj = itemObj["wholesale"]!!.jsonObject.toMutableMap()
            wholesaleMap.forEach { oldKey, newKey -> 
                if (oldKey != newKey){
                    wholesaleObj[newKey] = wholesaleObj.remove(oldKey)!!
                }
            }
            itemObj["wholesale"] = JsonObject(wholesaleObj)
            
            val retailObj = itemObj["retail"]!!.jsonObject.toMutableMap()
            retailMap.forEach { oldKey, newKey -> 
                if (oldKey != newKey){
                    retailObj[newKey] = retailObj.remove(oldKey)!!
                }
            }
            
            if (narahenpitaObj != null){
                retailObj["narahenpita"] = narahenpitaObj
            }
            itemObj["retail"] = JsonObject(retailObj)
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