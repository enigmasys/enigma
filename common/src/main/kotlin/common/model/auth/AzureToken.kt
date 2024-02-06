package common.model.auth

data class AzureToken(
    val access_token: String,
    val expires_in: Int,
    val ext_expires_in: Int,
    val token_type: String,
)
