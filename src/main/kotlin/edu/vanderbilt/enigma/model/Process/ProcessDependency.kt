package edu.vanderbilt.enigma.model.Process

data class ProcessDependency(
    val endIndex: Int,
    val isFunctionProcess: Boolean,
    val isInput: Boolean,
    val processId: String,
    val processType: String,
    val startIndex: Int,
    val version: Int
)