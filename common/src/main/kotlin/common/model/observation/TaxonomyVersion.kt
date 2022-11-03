package common.model.observation

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class TaxonomyVersion(
    val branch: String?,
    val commit: String?,
    val id: String?,
    val tag: String?,
    val url: String?
)