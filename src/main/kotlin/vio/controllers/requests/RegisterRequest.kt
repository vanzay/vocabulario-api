package vio.controllers.requests

import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Email

class RegisterRequest(
    @field:Email
    val email: String,
    @field:Length(min = 5)
    val password: String,
    val langIso2: String
)
