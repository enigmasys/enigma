package common.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.ALWAYS)
data class DataLakeFile(
    val hash: Any?,
    val length: Any?,
    val name: String,
    val sasUrl: String
)