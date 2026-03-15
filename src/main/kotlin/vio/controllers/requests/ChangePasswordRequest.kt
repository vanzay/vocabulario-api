package vio.controllers.requests

import org.hibernate.validator.constraints.Length

class ChangePasswordRequest(
    val uid: Int,
    val token: String,
    @field:Length(min = 5)
    val password: String
)
