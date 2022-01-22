package vio.services

import org.springframework.stereotype.Service
import vio.domain.*

@Service
class UserBookService(
    private val userBookRepository: UserBookRepository,
    private val userPhraseRepository: UserPhraseRepository
) {

    fun attachBookToUser(book: Book, user: User): UserBook {
        var userBook = userBookRepository.findByUserAndBook(user, book)
        if (userBook == null) {
            userBook = UserBook(
                user = user,
                book = book
            )
            userBookRepository.save(userBook)
        }
        return userBook
    }

    fun getSummary(book: Book, user: User): UserBook {
        val userBook = userBookRepository.findByUserAndBook(user, book)
        if (userBook != null) {
            return userBook
        }

        val comfort = userPhraseRepository.getComfort(book, user)
        val summary = userPhraseRepository.getSummary(book, user)
        return UserBook(
            comfort = comfort.toShort(),
            totalPhrases = summary.totalPhrases.toInt(),
            knownPhrases = summary.knownPhrases.toInt(),
            book = book,
            user = user
        )
    }

    fun updateAllSummary(user: User, currentBook: Book?) {
        val userBooks = userBookRepository.getUserBooks(user, currentBook?.language)

        // update current book on the fly
        if (currentBook != null) {
            val userBook = userBooks.find { it.book == currentBook }
            if (userBook != null) {
                updateSummary(userBook)
            }
        }

        // update rest of books asynchronously
        userBooks
            .filter { currentBook == null || it.book != currentBook }
            .forEach {
                // TODO push to redis/mq and update only after some period
                updateSummary(it)
            }
    }

    fun updateSummary(userBook: UserBook) {
        // TODO optimization (stored procedure?)
        val comfort = userPhraseRepository.getComfort(userBook.book, userBook.user)
        val summary = userPhraseRepository.getSummary(userBook.book, userBook.user)
        val updatedUserBook = userBook.copy(
            comfort = comfort.toShort(),
            totalPhrases = summary.totalPhrases.toInt(),
            knownPhrases = summary.knownPhrases.toInt()
        )
        userBookRepository.save(updatedUserBook)
    }
}
