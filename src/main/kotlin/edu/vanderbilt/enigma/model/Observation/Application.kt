package edu.vanderbilt.enigma.model.Observation

import edu.vanderbilt.enigma.model.Inputs
import edu.vanderbilt.enigma.model.RuntimeConfig

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