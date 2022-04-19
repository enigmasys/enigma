package edu.vanderbilt.enigma.model.observation

import com.fasterxml.jackson.annotation.JsonInclude
import edu.vanderbilt.enigma.model.process.ProcessDependency

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class UploadObservationObject(
    var endTime: String?,
    var index: Int,
    var isFunction: Boolean?,
    var isMeasure: Boolean?,
    var observerId: String?,
    var applicationDependencies: List<ApplicationDependency>? = emptyList(),
    var processDependencies: List<ProcessDependency>? = emptyList(),
    var processId: String,
    var processType: String?,
    var startTime: String?,
    var version: Int?,
    var `data`: List<Any>? = emptyList(),
    var dataFiles: List<String>?= emptyList()
    )