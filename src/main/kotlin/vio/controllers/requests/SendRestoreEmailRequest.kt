package vio.controllers.requests

import javax.validation.constraints.Email

class SendRestoreEmailRequest(
    @field:Email
    var email: String
)
