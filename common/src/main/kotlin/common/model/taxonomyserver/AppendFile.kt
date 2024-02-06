package common.model.taxonomyserver

import com.fasterxml.jackson.annotation.JsonProperty

data class AppendFile(
    @JsonProperty("name")
    val name: String,
    @JsonProperty("params")
    val params: AppendParams,
)
