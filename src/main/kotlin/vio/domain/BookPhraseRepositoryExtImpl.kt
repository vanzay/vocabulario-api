package vio.domain

import org.springframework.stereotype.Repository
import vio.domain.results.BookPhraseEntry
import vio.domain.results.PhraseData
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.Tuple

@Repository
class BookPhraseRepositoryExtImpl(
    @PersistenceContext
    private val em: EntityManager
) : BookPhraseRepositoryExt {

    override fun getPhrases(
        user: User?,
        language: Language,
        book: Book,
        inDictionary: Boolean,
        offset: Int,
        size: Int
    ): Collection<PhraseData> {
        val idList = getTopPhraseIdList(book, user, inDictionary, offset, size)
        val idMap = idList.withIndex().associate { it.value to it.index }

        val basePhrases = getPhrasesData(book, idList, true, user, language)
        val result = basePhrases
            .sortedBy { idMap[it.phraseId] }
            .associate { it.phraseId to PhraseData(it, ArrayList()) }

        val phrases = getPhrasesData(book, result.keys, false, user, language)
        phrases.forEach {
            result[it.basePhraseId]?.phrases?.add(it)
        }

        return result.values
    }

    private fun getTopPhraseIdList(book: Book, user: User?, inDictionary: Boolean, offset: Int, size: Int): List<Int> {
        val topPhrases = em.createNativeQuery(
            """
            SELECT distinct(coalesce(p.base_phrase_id, p.phrase_id)) AS phrase_id, bp.group_frequency
            FROM book_phrase bp
            JOIN phrase p ON p.phrase_id = bp.phrase_id AND p.visible = true AND p.base_phrase_id IS NULL
            ${if (user == null) "" else "LEFT JOIN user_phrase up ON up.phrase_id = p.phrase_id AND up.user_id = ${user.id}"}
            WHERE bp.book_id = ${book.id} ${if (user == null) "" else "AND up.on_studying IS ${if (inDictionary) "NOT NULL" else "NULL"}"}
            ORDER BY bp.group_frequency DESC
            OFFSET $offset
            LIMIT $size
            """,
            Tuple::class.java
        ).resultList as List<Tuple>

        return topPhrases.map { it[0] as Int }
    }

    private fun getPhrasesData(
        book: Book,
        idList: Collection<Int>,
        baseForm: Boolean,
        user: User?,
        language: Language
    ): List<BookPhraseEntry> {
        return if (idList.isEmpty()) emptyList() else em.createNativeQuery(
            """
            SELECT bp.frequency AS frequency,
                bp.group_frequency AS groupFrequency,
                bp.group_order AS groupOrder,
                p.phrase_id AS phraseId,
                (p.base_phrase_id IS NULL) AS baseForm,
                p.base_phrase_id AS basePhraseId,
                p.term AS term,
                p.group_number AS groupNumber,
                ${if (user == null) "NULL" else "up.on_studying"} AS onStudying,
                ${if (user == null) "NULL" else "up.translation"} AS userTranslation,
                t.term AS translation
            FROM phrase p
            JOIN book_phrase bp ON bp.phrase_id = p.phrase_id AND bp.book_id = ${book.id}
            ${if (user == null) "" else "LEFT JOIN user_phrase up ON up.phrase_id = p.phrase_id AND up.user_id = ${user.id}"}
            LEFT JOIN translation t ON t.phrase_id = p.phrase_id AND t.language_id = ${language.id}
            WHERE ${if (baseForm) "p.phrase_id" else "p.base_phrase_id"} IN (${idList.joinToString(",")})
            ${if (baseForm) "" else "ORDER BY p.term"}
            """,
            "BookPhraseMapping"
        ).resultList as List<BookPhraseEntry>
    }
}
