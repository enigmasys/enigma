package common.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Taxonomy(
    @JsonProperty("branch")
    val branch: String?,
    @JsonProperty("commit")
    val commit: String?,
    @JsonProperty("id")
    val id: String?,
    @JsonProperty("url")
    val url: String?,
)
