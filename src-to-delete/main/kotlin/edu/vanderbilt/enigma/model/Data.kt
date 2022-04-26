package edu.vanderbilt.enigma.model

data class Data(
    val Attachments: List<String>,
    val Collection: Collection,
    val Guid: String,
    val History: List<History>,
    val Label: String,
    val Notes: String,
    val Quantity: Int,
    val Sex: Int,
    val Status: Int,
    val Type: Int
)