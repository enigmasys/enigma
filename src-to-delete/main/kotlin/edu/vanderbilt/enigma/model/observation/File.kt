package edu.vanderbilt.enigma.model.observation

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.ALWAYS)
data class File(
    val hash: Any?,
    val length: Any?,
    val name: String,
    val sasUrl: String
)