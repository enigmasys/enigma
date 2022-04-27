package common.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.invoke.MethodHandles

private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().`package`.name)

public fun prettyJsonPrint(uploadData: Any) {
    val mapper = ObjectMapper()
    try {
//        val jsonObject: Any = mapper.readValue(uploadData, Any::class.java)
        val prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(uploadData)
        logger.info(prettyJson)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
