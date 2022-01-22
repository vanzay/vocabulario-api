package vio.services

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import vio.domain.User
import vio.domain.UserRepository
import vio.utils.DigestUtils


@Service
class UserService(
    private val userRepository: UserRepository,
    private val languageService: LanguageService
) {

    fun create(email: String, password: String, langIso2: String): User? {
        try {
            val language = languageService.getOrDefault(langIso2)

            val user = User(
                email = email,
                password = getPasswordHash(password),
                language = language
            )
            userRepository.saveAndFlush(user)

            return user
        } catch (ex: DataIntegrityViolationException) {
            return null
        }
    }

    fun find(username: String, password: String): User? {
        val user = userRepository.findByEmail(username)
        return if (user != null && checkPassword(password, user)) user else null
    }

    fun getRestoreToken(user: User): String {
        // TODO improve
        //  either extract fixed timestamp (one day etc) from Date(), so the link will be available for specified period
        //  or add Date() and store token in user_token table
        return DigestUtils.sha256("${user.id}${user.password}")
    }

    fun find(uid: Int, token: String): User? {
        val user = userRepository.findById(uid).orElse(null)
        return if (user != null && token == getRestoreToken(user)) user else null
    }

    fun updatePasswordAndConfirm(user: User, password: String) {
        val updatedUser = user.copy(
            password = getPasswordHash(password),
            confirmed = true
        )
        userRepository.save(updatedUser)
    }

    private fun getPasswordHash(password: String) = BCryptPasswordEncoder().encode(password)

    private fun checkPassword(password: String, user: User) =
        BCryptPasswordEncoder().matches(password, user.password)
}
