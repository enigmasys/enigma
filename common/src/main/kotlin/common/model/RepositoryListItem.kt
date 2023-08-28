package common.model


import com.fasterxml.jackson.annotation.JsonProperty

data class RepositoryListItem(
    @JsonProperty("displayName")
    val displayName: String,
    @JsonProperty("id")
    val id: String,
    @JsonProperty("taxonomyVersion")
    val taxonomyVersion: Taxonomy
)