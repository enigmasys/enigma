package edu.vanderbilt.enigma.model

data class EgressDataFiles(
    val directoryId: String,
    val expiresOn: String,
    val files: List<File>,
    val processId: String,
    val transferId: String
)