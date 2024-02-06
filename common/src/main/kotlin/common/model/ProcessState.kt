package common.model

data class ProcessState(
    val isFunction: Boolean,
    val lastVersionIndex: Int,
    val numObservations: Int,
    val processId: String,
    val processType: String,
)
