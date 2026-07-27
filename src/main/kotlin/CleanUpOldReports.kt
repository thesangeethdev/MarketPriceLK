package com.sangeeth

import java.io.File

fun cleanupOldReports(){
    val datadir = File("data")
    if (!datadir.exists()) return

    val files = datadir.listFiles{_, name ->
        name.startsWith("price_") && name.endsWith(".json")
    }
        ?.sortedByDescending { it.name }
        ?:emptyList()

    if (files.size>3){
        files.drop(3).forEach { it.delete() }
    }
}