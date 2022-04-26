package edu.vanderbilt.enigma.util

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException



public fun prettyJsonPrint(uploadData: Any) {
    val mapper = ObjectMapper()
    try {
//        val jsonObject: Any = mapper.readValue(uploadData, Any::class.java)
        val prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(uploadData)
        println(prettyJson)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
