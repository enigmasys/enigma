package edu.vanderbilt.enigma.model

data class Application(
    val funName: String,
    val hash: String,
    val index: Int,
    val inputs: Inputs,
    val processId: String,
    val processType: String,
    val runtimeConfig: RuntimeConfig,
    val version: Int
)