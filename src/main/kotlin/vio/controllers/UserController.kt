package vio.controllers

import org.springframework.context.MessageSource
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import vio.controllers.requests.*
import vio.controllers.responses.CheckEmailResponse
import vio.controllers.responses.LoginResponse
import vio.controllers.responses.RegisterResponse
import vio.domain.UserRepository
import vio.errors.ApiException
import vio.services.JwtService
import vio.services.MailService
import vio.services.UserService
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping("/v1/user")
class UserController(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val userService: UserService,
    private val mailService: MailService,
    private val messageSource: MessageSource
) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): LoginResponse {
        // TODO recaptcha
        val user = userService.find(loginRequest.email, loginRequest.password)
            ?: throw ApiException("USER_NOT_FOUND")
        val accessToken = jwtService.issueToken(user)
        return LoginResponse(user.email, accessToken)
    }

    @PostMapping("/logout")
    fun logout() {
        // TODO revoke jwt
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): RegisterResponse {
        // TODO recaptcha
        val user = userService.create(registerRequest.email, registerRequest.password, registerRequest.langIso2)
            ?: throw ApiException("USER_EXISTS_ALREADY")
        val accessToken = jwtService.issueToken(user)
        return RegisterResponse(user.email, accessToken)
    }

    @PostMapping("/check-email")
    fun checkEmail(@Valid @RequestBody checkEmailRequest: CheckEmailRequest): CheckEmailResponse {
        // TODO recaptcha
        val user = userRepository.findByEmail(checkEmailRequest.email)
        return CheckEmailResponse(user != null)
    }

    @PostMapping("/send-restore-email")
    fun sendRestoreEmail(@Valid @RequestBody sendRestoreEmailRequest: SendRestoreEmailRequest) {
        // TODO recaptcha
        val user = userRepository.findByEmail(sendRestoreEmailRequest.email)
        if (user != null) {
            val locale = Locale(user.language.iso2)
            mailService.send(
                user.email,
                messageSource.getMessage("mail.restore.subject", null, locale),
                messageSource.getMessage(
                    "mail.restore.body",
                    arrayOf(user.id.toString(), userService.getRestoreToken(user)),
                    locale
                )
            )
        }
    }

    @PostMapping("/change-password")
    fun changePassword(@Valid @RequestBody changePasswordRequest: ChangePasswordRequest) {
        // TODO recaptcha
        val user = userService.find(changePasswordRequest.uid, changePasswordRequest.token)
            ?: throw ApiException("INVALID_TOKEN")
        userService.updatePasswordAndConfirm(user, changePasswordRequest.password)
    }
}
