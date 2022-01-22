package vio.controllers

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import vio.domain.Book
import vio.domain.BookRepository
import vio.domain.SearchAttempt
import vio.domain.SearchAttemptRepository
import vio.utils.DbUtils.prepareFullTextQuery

@RestController
@RequestMapping("/v1/search")
class SearchController(
    private val bookRepository: BookRepository,
    private val searchAttemptRepository: SearchAttemptRepository,
    @Value("\${search.page.size}")
    private val pageSize: Int,
    @Value("\${search.page.results.max}")
    private val searchMaxResults: Int,
    @Value("\${search.page.autocomplete.results.max}")
    private val autocompleteMaxResults: Int
) {

    @GetMapping("/books")
    fun getBooks(query: String?, langIso2: String?, page: Int): List<Book> {
        val books = if (query.isNullOrBlank()) {
            getBooks(langIso2, page)
        } else if (page > 0) {
            // TODO pagination for search query
            emptyList()
        } else {
            val searchAttempt = SearchAttempt(
                queryText = query.trim().take(128)
            )
            searchAttemptRepository.save(searchAttempt)

            bookRepository.search(prepareFullTextQuery(query.trim()), searchMaxResults)
        }
        return books
    }

    @GetMapping("/books-for-autocomplete")
    fun getBooksForAutocomplete(query: String): List<Book> {
        return if (query.isBlank()) emptyList() else
            bookRepository.search(prepareFullTextQuery(query.trim()), autocompleteMaxResults)
    }

    private fun getBooks(langIso2: String?, page: Int): List<Book> {
        val pageRequest = PageRequest.of(page, pageSize, Sort.Direction.DESC, "addedDate")
        return bookRepository.getBooks(langIso2, pageRequest)
    }
}
