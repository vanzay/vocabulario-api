package vio.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import vanzay.text.analyze.Entry
import vanzay.text.analyze.Vocabulary
import vio.domain.*
import vio.utils.DbUtils.getPartitionKey
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Service
@Transactional
class BookService(
    private val bookRepository: BookRepository,
    private val bookPhraseRepository: BookPhraseRepository,
    private val languageRepository: LanguageRepository,
    private val unexpectedWordRepository: UnexpectedWordRepository,
    private val userBookRepository: UserBookRepository,
    @PersistenceContext
    private val entityManager: EntityManager
) {

    companion object {
        const val BATCH_SIZE = 1000
    }

    fun getBook(id: Int, user: User?): Book? {
        val book = bookRepository.findById(id).orElse(null)
        return if (checkBook(book, user)) book else null
    }

    fun checkBook(book: Book?, user: User?): Boolean {
        return book != null && (book.common || user == null || userBookRepository.findByUserAndBook(user, book) != null)
    }

    fun saveBook(
        author: String?,
        title: String,
        contentHash: String,
        translated: Boolean,
        coverUrl: String?,
        contentUrl: String?,
        audioContentUrl: String?,
        vocabulary: Vocabulary
    ): Book {
        val language = languageRepository.findByIso3(vocabulary.language.iso3)
        val now = Date()

        val phrases = vocabulary.phrases
        val book = Book(
            author = author?.take(64),
            title = title.take(128),
            addedDate = now,
            contentHash = contentHash,
            totalWords = vocabulary.phrasesTotal.toLong(),
            uniqueWords = phrases.size,
            uniqueGroups = phrases.size,
            common = false,
            translated = translated,
            coverUrl = coverUrl,
            contentUrl = contentUrl,
            audioContentUrl = audioContentUrl,
            language = language!!
        )
        bookRepository.save(book)

        val partKey = getPartitionKey(now)
        savePhrases(book, phrases, partKey)

        bookRepository.calculateGroupOrder(book.id)

        saveUnexpectedWords(book, vocabulary.unexpectedWords)

        return book
    }

    private fun savePhrases(book: Book, data: Collection<Entry>, partKey: Int) {
        data.chunked(BATCH_SIZE)
            .forEach { chunk ->
                val bookPhrases = chunk.map {
                    val phrase = entityManager.getReference(Phrase::class.java, it.id)
                    BookPhrase(
                        book = book,
                        phrase = phrase,
                        frequency = it.frequency,
                        groupFrequency = 0,
                        groupOrder = 0,
                        partKey = partKey
                    )
                }
                bookPhraseRepository.saveAll(bookPhrases)
            }
    }

    private fun saveUnexpectedWords(book: Book, data: Collection<Entry>) {
        data.filter { !it.term.matches(".*\\d.*".toRegex()) }
            .chunked(BATCH_SIZE)
            .forEach { chunk ->
                val unexpectedWords = chunk
                    .map {
                        UnexpectedWord(
                            term = it.term.take(64),
                            frequency = it.frequency,
                            book = book
                        )
                    }
                unexpectedWordRepository.saveAll(unexpectedWords)
            }
    }
}
