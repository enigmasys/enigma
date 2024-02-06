package common.model.observation

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class FileUrlInfoItem(
    @JsonProperty("files")
    val files: List<FileX>,
    @JsonProperty("id")
    val id: String,
    @JsonProperty("repoId")
    val repoId: String,
)
