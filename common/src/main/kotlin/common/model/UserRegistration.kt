package common.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class UserRegistration(
    val isAdministrator: Boolean,
    val isRegistered: Int,
    val isRegistrationApprover: Boolean,
    val userId: String
)