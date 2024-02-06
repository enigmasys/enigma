package common.model.process

data class ProcessOwnedItem(
    val description: String,
    val isFunction: Boolean,
    val processId: String,
    val processType: String,
)
