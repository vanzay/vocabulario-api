package vio.controllers.requests

import jakarta.validation.constraints.Email

class CheckEmailRequest(
    @field:Email
    val email: String
)
