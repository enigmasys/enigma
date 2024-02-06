package common.model

data class Directory(
    val createdOn: String,
    val creatorId: String,
    val directoryId: String,
    val expiresInMins: Int,
    val isUploadDir: Boolean,
    val processId: String,
    val sasUrl: String,
)
