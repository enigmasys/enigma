package common.model.User

data class ProcessUserInformation(
    val isFunction: Boolean,
    val isOwner: Boolean,
    val principalId: String,
    val processId: String,
    val processType: String,
)
