package common.model.observation


import com.fasterxml.jackson.annotation.JsonProperty

data class FileX(
    @JsonProperty("name")
    val name: String,
    @JsonProperty("url")
    val url: String
)