package vio.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import vio.controllers.responses.ErrorResponse
import vio.errors.ApiException
import vio.errors.UnauthorizedException

@ControllerAdvice
class CustomizedExceptionHandling : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        return ResponseEntity.badRequest().build()
    }

    @ExceptionHandler(ApiException::class)
    fun handleException(ex: ApiException, webRequest: WebRequest): ResponseEntity<Any> {
        log.error("API exception", ex)
        return ResponseEntity.badRequest().body(ErrorResponse(ex.code.name))
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleException(ex: UnauthorizedException, webRequest: WebRequest): ResponseEntity<Any> {
        log.error("Unauthorized access", ex)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }
}
