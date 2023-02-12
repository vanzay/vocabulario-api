package vio.domain

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import vio.domain.results.DictionarySummary
import vio.domain.results.LanguageEntry
import vio.domain.results.UserBookSummary
import java.util.*

interface UserPhraseRepository : JpaRepository<UserPhrase, Int> {

    fun findByUserAndPhrase(user: User, phrase: Phrase): UserPhrase?

    @Query(
        """
        SELECT up
        FROM UserPhrase up
        JOIN FETCH up.phrase p
        WHERE up.user = :user
            AND p.language = :language
            AND (:term IS null OR p.term LIKE :term)
            AND (:onStudying IS null OR up.onStudying = :onStudying)
        """
    )
    fun getPhrases(
        user: User,
        language: Language,
        term: String?,
        onStudying: Boolean?,
        pageable: Pageable
    ): List<UserPhrase>

    @Query(
        """
        SELECT up
        FROM UserPhrase up
        JOIN FETCH up.phrase p
        WHERE up.user = :user
            AND p.language = :language
            AND up.onStudying = true
            AND (:book IS null OR p.id IN (SELECT bp.phrase.id FROM BookPhrase bp WHERE bp.book = :book))
        """
    )
    fun getPhrasesForTraining(
        user: User,
        book: Book?,
        language: Language,
        pageable: Pageable
    ): List<UserPhrase>

    @Query(
        """
        SELECT up
        FROM UserPhrase up
        JOIN FETCH up.phrase p
        WHERE up.user = :user
            AND up.id IN (:idList)
        """
    )
    fun getExportData(user: User, idList: List<Int>): List<UserPhrase>

    @Query(
        """
        SELECT new vio.domain.results.LanguageEntry(
            l.iso2,
            l.nativeName,
            count(l.id)
        )
        FROM UserPhrase up
        JOIN Phrase p ON p.id = up.phrase.id
        JOIN Language l ON l.id = p.language.id
        WHERE up.user = :user
        GROUP BY l.id
        """
    )
    fun getAvailableLanguages(user: User): List<LanguageEntry>

    @Query(
        """
        SELECT new vio.domain.results.DictionarySummary(
            sum(CASE WHEN up.onStudying = false THEN 1 ELSE 0 END),
            sum(CASE WHEN up.onStudying = true THEN 1 ELSE 0 END)
        )
        FROM UserPhrase up
        JOIN Phrase p ON p.id = up.phrase.id
        JOIN Language l ON l.id = p.language.id AND l.id = :#{#language.id}
        WHERE up.user = :user
        """
    )
    fun getDictionarySummary(user: User, language: Language): DictionarySummary

    @Query(
        """
        SELECT coalesce(sum(bp.frequency), 0) * 100 / :#{#book.totalWords}
        FROM BookPhrase bp
        JOIN UserPhrase up ON up.phrase = bp.phrase AND up.user = :user
        WHERE bp.book = :book
        """
    )
    fun getComfort(book: Book, user: User): Number

    @Query(
        """
        SELECT new vio.domain.results.UserBookSummary(
            count(up.id),
            coalesce(sum(CASE WHEN up.onStudying = false THEN 1 ELSE 0 END), 0)
        )
        FROM UserPhrase up
        WHERE up.user = :user AND up.phrase.id IN (
            SELECT DISTINCT(coalesce(p.basePhrase.id, p.id))
            FROM UserPhrase up
            JOIN BookPhrase bp ON bp.phrase = up.phrase AND bp.book = :book
            JOIN Phrase p ON p.id = up.phrase.id
            WHERE up.user = :user
        )
        """
    )
    fun getSummary(book: Book, user: User): UserBookSummary

    @Modifying
    @Query(
        value = """
            INSERT INTO user_phrase(part_key, on_studying, added_date, user_id, phrase_id)
            SELECT :partKey, :onStudying, :timestamp, :userId, p.phrase_id
            FROM phrase p
            LEFT JOIN user_phrase up ON up.phrase_id = p.phrase_id AND up.user_id = :userId
            WHERE p.phrase_id IN (:idList) AND up.phrase_id IS NULL""", nativeQuery = true
    )
    fun addBaseFormsToVocabulary(
        userId: Int,
        idList: List<Int>,
        partKey: Int,
        onStudying: Boolean,
        timestamp: Date
    )

    @Modifying
    @Query(
        value = """
            INSERT INTO user_phrase(part_key, on_studying, added_date, user_id, phrase_id)
            SELECT :partKey, false, :timestamp, :userId, p.phrase_id
            FROM phrase p
            LEFT JOIN user_phrase up ON up.phrase_id = p.phrase_id AND up.user_id = :userId
            WHERE p.base_phrase_id IN (:idList) AND up.phrase_id IS NULL""", nativeQuery = true
    )
    fun addRestFormsToVocabulary(
        userId: Int,
        idList: List<Int>,
        partKey: Int,
        timestamp: Date
    )

    @Modifying
    @Query(
        """
        UPDATE UserPhrase
        SET memoryPoints = memoryPoints - :maxAnswerPoints
        WHERE onStudying = true AND memoryPoints > :maxAnswerPoints AND lastActivity < :limitDate
        """
    )
    fun refreshMemoryPoints(maxAnswerPoints: Int, limitDate: Date)

    @Modifying
    @Query(
        """
        UPDATE UserPhrase
        SET auditionPoints = auditionPoints - :maxAnswerPoints
        WHERE onStudying = true AND auditionPoints > :maxAnswerPoints AND lastActivity < :limitDate
        """
    )
    fun refreshAuditionPoints(maxAnswerPoints: Int, limitDate: Date)

    @Modifying
    @Query("UPDATE UserPhrase SET onStudying = :onStudying WHERE user = :user AND id IN (:idList)")
    fun updateOnStudying(idList: List<Int>, onStudying: Boolean, user: User)

    @Modifying
    @Transactional
    @Query("UPDATE UserPhrase SET translation = :translation WHERE user = :user AND id = :userPhraseId")
    fun updateTranslation(user: User, userPhraseId: Int, translation: String)

    @Modifying
    @Query("DELETE FROM UserPhrase WHERE user = :user AND id IN (:idList)")
    fun deletePhrases(idList: List<Int>, user: User): Int
}
