package common.model.observation

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class TaxonomyData(
    var displayName: String?,
    var taxonomyTags: List<Any>?,
    val taxonomyVersion: TaxonomyVersion?
)