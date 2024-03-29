package common.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.ALWAYS)
data class EgressData(
    val directoryId: String,
    val expiresOn: String,
    val files: List<DataLakeFile>?,
    val processId: String,
    val transferId: Any?
)

