package common.model.observation

import common.model.observation.Application

data class ApplicationDependency(
    val application: Application,
    val outputName: String
)