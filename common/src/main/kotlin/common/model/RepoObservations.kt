package common.model


import com.fasterxml.jackson.annotation.JsonProperty

data class RepoObservations(
    @JsonProperty("children")
    val children: List<Children>?,
    @JsonProperty("displayName")
    val displayName: String?,
    @JsonProperty("id")
    val id: String?,
    @JsonProperty("taxonomyTags")
    val taxonomyTags: List<Any>?
)