package common.model.observation

import common.model.Inputs
import common.model.RuntimeConfig

data class Application(
    val funName: String,
    val hash: String,
    val index: Int,
    val inputs: Inputs,
    val processId: String,
    val processType: String,
    val runtimeConfig: RuntimeConfig,
    val version: Int,
)
