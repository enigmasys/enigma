package edu.vanderbilt.enigma.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class TransferStat(
    val files: List<String>,
    val lastUpdate: String,
    val operation: String,
    val runDurationMs: Any,
    val runEnd: Any,
    val runStart: String,
    val status: String
)