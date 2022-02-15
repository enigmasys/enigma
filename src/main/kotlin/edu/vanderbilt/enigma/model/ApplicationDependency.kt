package edu.vanderbilt.enigma.model

import edu.vanderbilt.enigma.model.Application

data class ApplicationDependency(
    val application: Application,
    val outputName: String
)