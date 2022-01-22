package vio.controllers.requests

import javax.validation.constraints.Email

class CheckEmailRequest(
    @field:Email
    val email: String
)
