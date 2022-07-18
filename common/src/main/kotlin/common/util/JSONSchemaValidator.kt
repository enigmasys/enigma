package common.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersionDetector
import com.networknt.schema.ValidationMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL
import java.util.function.Consumer


enum class ResourceSourceType {
    FILE, RESOURCE, URL
}

@Component
class JSONSchemaValidator {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun inputStreamFromClasspath(path: String): InputStream {
        return Thread.currentThread().contextClassLoader.getResourceAsStream(path)
    }

    fun inputStreamFromFilepath(path: String): InputStream {
        return FileInputStream(path)
    }

    fun inputStreamFromURL(path: String): InputStream {
        val url = URL(path)
        return url.openStream()
    }

    fun getJsonSchemaFromJsonNodeAutomaticVersion(jsonNode: JsonNode?): JsonSchema? {
        val factory = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(jsonNode))
        return factory.getSchema(jsonNode)
    }

    fun validate(
        schemasourcetype: ResourceSourceType,
        jsonSchemaPath: String,
        metadataPath: String
    ): Boolean {
        val objectMapper = ObjectMapper()
        val schemaStream = when (schemasourcetype) {
            ResourceSourceType.FILE -> inputStreamFromFilepath(jsonSchemaPath)
            ResourceSourceType.URL -> inputStreamFromURL(jsonSchemaPath)
            ResourceSourceType.RESOURCE -> inputStreamFromClasspath(jsonSchemaPath)
        }
        val jsonStream = inputStreamFromFilepath(metadataPath)
        return validateSchema(objectMapper, jsonStream, schemaStream)
    }

    private fun validateSchema(
        objectMapper: ObjectMapper,
        jsonStream: InputStream,
        schemaStream: InputStream
    ): Boolean {

//        val schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)

        val json: JsonNode = objectMapper.readTree(jsonStream)

        val schemaNode: JsonNode = objectMapper.readTree(schemaStream)

        val schema = getJsonSchemaFromJsonNodeAutomaticVersion(schemaNode)


//        val schema: JsonSchema = schemaFactory.getSchema(schemaStream)

        val validationResult: Set<ValidationMessage> = schema!!.validate(json)
        if (validationResult.isEmpty()) {
            log.info("no validation errors :-)")
            return true

        } else {

//            TODO("Need to emit error here.. Also, need to properly upload the data once this is success")
            validationResult.forEach(Consumer { vm: ValidationMessage ->
                println(
                    vm.message
                )
            })
            return false
        }
    }
}