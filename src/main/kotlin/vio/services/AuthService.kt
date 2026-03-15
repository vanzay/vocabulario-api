package vio.services

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import vio.domain.User
import vio.domain.UserRepository

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService
) {

    companion object {
        private const val AUTHORIZATION = "Authorization"
    }

    fun getUser(request: HttpServletRequest): User? {
        val token = getTokenFromRequest(request) ?: return null
        val claims = jwtService.validateToken(token)
        val email = claims["sub"] as String
        return userRepository.findByEmail(email)
    }

    private fun getTokenFromRequest(request: HttpServletRequest): String? {
        val bearer = request.getHeader(AUTHORIZATION)
        return if (!bearer.isNullOrBlank() && bearer.startsWith("Bearer ")) bearer.substring(7) else null
    }
}
