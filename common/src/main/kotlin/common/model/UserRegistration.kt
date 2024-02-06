package common.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class UserRegistration(
    val isAdministrator: Boolean,
    val isRegistered: String,
    val isRegistrationApprover: Boolean,
    val userId: String,
    val permission: String,
)
