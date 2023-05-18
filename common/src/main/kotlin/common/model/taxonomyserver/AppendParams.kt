package common.model.taxonomyserver


import com.fasterxml.jackson.annotation.JsonProperty

data class AppendParams(
    @JsonProperty("headers")
    val appendHeaders: AppendHeaders,
    @JsonProperty("method")
    val method: String,
    @JsonProperty("url")
    val url: String
)