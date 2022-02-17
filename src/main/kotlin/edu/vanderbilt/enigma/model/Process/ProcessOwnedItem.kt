package edu.vanderbilt.enigma.model.Process

data class ProcessOwnedItem(
    val description: String,
    val isFunction: Boolean,
    val processId: String,
    val processType: String
)