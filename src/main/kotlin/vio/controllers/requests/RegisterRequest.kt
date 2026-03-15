package vio.controllers.requests

import jakarta.validation.constraints.Email
import org.hibernate.validator.constraints.Length

class RegisterRequest(
    @field:Email
    val email: String,
    @field:Length(min = 5)
    val password: String,
    val langIso2: String
)
