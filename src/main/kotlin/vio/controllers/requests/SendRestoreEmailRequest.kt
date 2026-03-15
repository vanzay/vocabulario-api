package vio.controllers.requests

import jakarta.validation.constraints.Email

class SendRestoreEmailRequest(
    @field:Email
    val email: String
)
