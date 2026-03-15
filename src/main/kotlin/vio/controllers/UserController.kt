package vio.controllers

import jakarta.validation.Valid
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
import vio.errors.ErrorCode
import vio.services.JwtService
import vio.services.MailService
import vio.services.UserService
import java.util.*


@RestController
@RequestMapping("/v1/user")
// TODO recaptcha
class UserController(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val userService: UserService,
    private val mailService: MailService,
    private val messageSource: MessageSource
) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): LoginResponse {
        val user = userService.find(loginRequest.email, loginRequest.password)
            ?: throw ApiException(ErrorCode.USER_NOT_FOUND)
        val accessToken = jwtService.issueToken(user)
        return LoginResponse(user.email, accessToken)
    }

    @PostMapping("/logout")
    fun logout() {
        // TODO revoke jwt
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): RegisterResponse {
        val user = userService.create(registerRequest.email, registerRequest.password, registerRequest.langIso2)
            ?: throw ApiException(ErrorCode.USER_EXISTS_ALREADY)
        val accessToken = jwtService.issueToken(user)
        return RegisterResponse(user.email, accessToken)
    }

    @PostMapping("/check-email")
    fun checkEmail(@Valid @RequestBody checkEmailRequest: CheckEmailRequest): CheckEmailResponse {
        val user = userRepository.findByEmail(checkEmailRequest.email)
        return CheckEmailResponse(user != null)
    }

    @PostMapping("/send-restore-email")
    fun sendRestoreEmail(@Valid @RequestBody sendRestoreEmailRequest: SendRestoreEmailRequest) {
        val user = userRepository.findByEmail(sendRestoreEmailRequest.email) ?: return

        val locale = Locale.of(user.language.name)
        mailService.sendWithQueue(
            user.email,
            messageSource.getMessage("mail.restore.subject", null, locale),
            messageSource.getMessage(
                "mail.restore.body",
                arrayOf(user.id.toString(), userService.getRestoreToken(user)),
                locale
            )
        )
    }

    @PostMapping("/change-password")
    fun changePassword(@Valid @RequestBody changePasswordRequest: ChangePasswordRequest) {
        val user = userService.find(changePasswordRequest.uid, changePasswordRequest.token)
            ?: throw ApiException(ErrorCode.INVALID_TOKEN)
        userService.updatePasswordAndConfirm(user, changePasswordRequest.password)
    }
}
