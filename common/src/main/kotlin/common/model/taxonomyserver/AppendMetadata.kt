package common.model.taxonomyserver

import com.fasterxml.jackson.annotation.JsonProperty
import common.model.observation.TaxonomyData

data class AppendMetadata(
    @JsonProperty("filenames")
    var filenames: List<String>,
    @JsonProperty("metadata")
    val metadata: TaxonomyData,
)
