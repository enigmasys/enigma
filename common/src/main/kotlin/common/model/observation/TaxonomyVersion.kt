package common.model.observation

import com.fasterxml.jackson.annotation.JsonInclude
import java.beans.ConstructorProperties

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class TaxonomyVersion
    @ConstructorProperties("branch", "commit", "id", "tag", "url")
    constructor(
        val branch: String?,
        val commit: String?,
        val id: String?,
        val tag: String?,
        val url: String?,
    )
