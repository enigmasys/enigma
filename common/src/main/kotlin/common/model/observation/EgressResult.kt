package common.model.observation

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.ALWAYS)
data class EgressResult(
    val directoryId: String,
    val expiresOn: String,
    val files: List<File>?,
    val processId: String,
    val transferId: String?,
)
