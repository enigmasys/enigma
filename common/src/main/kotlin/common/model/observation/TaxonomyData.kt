package common.model.observation

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.beans.ConstructorProperties

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class TaxonomyData @ConstructorProperties("displayName", "taxonomyTags","taxonomyVersion", "tags") constructor(
    var displayName: String?,
    var taxonomyTags: List<Any>?,
    val taxonomyVersion: TaxonomyVersion?,
    val tags:Any?
) {
    constructor(displayName: String) : this(displayName, null, null, null)
}