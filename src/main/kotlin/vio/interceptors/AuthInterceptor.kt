package vio.interceptors

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import vio.errors.InvalidTokenException
import vio.services.AuthService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthInterceptor(
    private val authService: AuthService
) : HandlerInterceptor {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (request.method == "OPTIONS") {
            return true
        }
        try {
            val user = authService.getUser(request)
            if (user != null) {
                request.setAttribute("user", user)
                return true
            }
        } catch (ite: InvalidTokenException) {
            log.error("", ite)
        }
        response.status = HttpStatus.UNAUTHORIZED.value()
        return false
    }
}
