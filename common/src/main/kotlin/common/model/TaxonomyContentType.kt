package common.model


import com.fasterxml.jackson.annotation.JsonProperty

data class TaxonomyContentType(
    @JsonProperty("contentTypes")
    val contentTypes: List<ContentTypeX>,
    @JsonProperty("name")
    val name: String
)