package common.model
import com.fasterxml.jackson.annotation.JsonProperty

data class Children(
    @JsonProperty("displayName")
    val displayName: String?,
    @JsonProperty("id")
    val id: String?,
    @JsonProperty("parentId")
    val parentId: String?,
    @JsonProperty("taxonomy")
    val taxonomy: Taxonomy?,
    @JsonProperty("taxonomyTags")
    val taxonomyTags: List<Any>?,
    @JsonProperty("time")
    val time: String?
)