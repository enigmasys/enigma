package common.model.taxonomyserver


import com.fasterxml.jackson.annotation.JsonProperty

data class AppendResponse(
    @JsonProperty("files")
    val appendFiles: List<AppendFile>,
    @JsonProperty("index")
    val index: Int
)