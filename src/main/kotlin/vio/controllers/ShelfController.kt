package vio.controllers

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import vanzay.text.exception.UnsupportedLanguageException
import vio.domain.BookRepository
import vio.domain.User
import vio.domain.UserBook
import vio.domain.UserBookRepository
import vio.errors.ApiException
import vio.services.BookService
import vio.services.ProcessorFactory
import vio.services.UserBookService
import vio.utils.DigestUtils
import vio.utils.FileFormat
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping("/v1/shelf")
class ShelfController(
    private val bookRepository: BookRepository,
    private val userBookRepository: UserBookRepository,
    private val bookService: BookService,
    private val userBookService: UserBookService,
    private val processorFactory: ProcessorFactory,
    @Value("\${shelf.page.size}")
    private val pageSize: Int,
    @Value("\${shelf.page.daily.uploadings.max}")
    private val maxDailyUploadings: Int
) {

    @GetMapping("/books")
    fun getBooks(page: Int, request: HttpServletRequest): List<UserBook> {
        val user = request.getAttribute("user") as User
        return getUserBooks(user, page)
    }

    @PostMapping("/upload")
    fun upload(
        file: MultipartFile,
        author: String?,
        title: String,
        translated: Int?,
        coverUrl: String?,
        contentUrl: String?,
        audioContentUrl: String?,
        request: HttpServletRequest
    ): UserBook {
        val user = request.getAttribute("user") as User

        val uploadingCount = getUploadingCount(user)
        if (uploadingCount >= maxDailyUploadings) {
            throw ApiException("UPLOAD_LIMIT_EXCEEDED")
        }

        if (file.isEmpty) {
            throw ApiException("INVALID_FILE")
        }

        val fileFormat = FileFormat.of(file.originalFilename, file.contentType)
        if (fileFormat == null) {
            throw ApiException("UNSUPPORTED_FILE_TYPE")
        }

        val contentData = file.bytes
        val contentHash = DigestUtils.sha256(contentData)

        try {
            var book = bookRepository.findByContentHash(contentHash)
            if (book == null) {
                val processor = processorFactory.getProcessor(fileFormat)
                processor.process(contentData)
                book = bookService.saveBook(
                    author,
                    (if (title.isNotBlank()) title else if (!processor.title.isNullOrEmpty()) processor.title else file.originalFilename)!!,
                    contentHash,
                    translated == 1,
                    coverUrl,
                    contentUrl,
                    audioContentUrl,
                    processor.vocabulary!!
                )
            }

            val userBook = userBookService.attachBookToUser(book, user)
            userBookService.updateSummary(userBook)

            return userBook
        } catch (ex: UnsupportedLanguageException) {
            throw ApiException("UNSUPPORTED_LANGUAGE")
        }
    }

    private fun getUploadingCount(user: User): Long {
        val date = Instant.now().minus(Duration.ofHours(24))
        return userBookRepository.countByUserAndAddedDateAfter(user, Date.from(date))
    }

    private fun getUserBooks(user: User, page: Int): List<UserBook> {
        val pageRequest = PageRequest.of(page, pageSize, Sort.Direction.DESC, "addedDate")
        return userBookRepository.findAllByUser(user, pageRequest)
    }
}
