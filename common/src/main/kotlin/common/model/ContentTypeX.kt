package common.model


import com.fasterxml.jackson.annotation.JsonProperty

data class ContentTypeX(
    @JsonProperty("name")
    val name: String,
    @JsonProperty("path")
    val path: String,
    @JsonProperty("url")
    val url: String
)