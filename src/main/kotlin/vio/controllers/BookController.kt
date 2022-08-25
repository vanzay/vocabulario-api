package vio.controllers

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import vio.controllers.responses.GetBookInfoResponse
import vio.domain.Book
import vio.domain.BookPhraseRepository
import vio.domain.User
import vio.domain.results.PhraseData
import vio.errors.ApiException
import vio.errors.InvalidTokenException
import vio.errors.UnauthorizedException
import vio.services.AuthService
import vio.services.BookService
import vio.services.LanguageService
import vio.services.UserBookService
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/v1/book")
class BookController(
    private val bookPhraseRepository: BookPhraseRepository,
    private val authService: AuthService,
    private val bookService: BookService,
    private val languageService: LanguageService,
    private val userBookService: UserBookService,
    @Value("\${book.page.size}")
    private val pageSize: Int
) {

    @GetMapping("/info/{bookId}")
    fun getInfo(@PathVariable bookId: Int, request: HttpServletRequest): GetBookInfoResponse {
        val user = getUser(request)
        val book = getBook(bookId, user)
        val summary = if (user == null) null else userBookService.getSummary(book, user)
        return GetBookInfoResponse(book, summary)
    }

    @GetMapping("/phrases")
    fun getPhrases(
        bookId: Int,
        inDictionary: Boolean,
        langIso2: String?,
        offset: Int,
        request: HttpServletRequest
    ): Collection<PhraseData> {
        val user = getUser(request)
        val book = getBook(bookId, user)
        val language = user?.language ?: languageService.getOrDefault(langIso2)
        return bookPhraseRepository.getPhrases(user, language, book, inDictionary, offset, pageSize)
    }

    private fun getUser(request: HttpServletRequest): User? {
        try {
            return authService.getUser(request)
        } catch (ite: InvalidTokenException) {
            throw UnauthorizedException()
        }
    }

    private fun getBook(id: Int, user: User?): Book {
        val book = bookService.getBook(id, user)
            ?: throw ApiException("BOOK_NOT_FOUND")
        if (!book.common && user == null) {
            throw UnauthorizedException()
        }
        return book
    }
}
