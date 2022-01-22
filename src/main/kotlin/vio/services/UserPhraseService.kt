package vio.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import vio.domain.*
import vio.utils.DbUtils.getPartitionKey
import java.sql.Timestamp
import java.util.*

@Service
@Transactional
class UserPhraseService(
    private val translationRepository: TranslationRepository,
    private val userPhraseRepository: UserPhraseRepository,
    private val userBookService: UserBookService
) {
    companion object {
        const val BATCH_SIZE = 1000
    }

    fun fillTranslations(userPhrases: List<UserPhrase>, language: Language) {
        val filteredUserPhrases = userPhrases.filter { it.translation.isNullOrBlank() }
        val idList = filteredUserPhrases.map { it.phrase.id }
        val translationMap =
            translationRepository.getTranslations(idList, language).associate { it.phrase.id to it.term }
        filteredUserPhrases.forEach {
            it.translation = translationMap[it.phrase.id]
        }
    }

    fun add(user: User, book: Book, idList: List<Int>, onStudying: Boolean) {
        if (idList.isEmpty()) {
            return
        }

        val partKey = getPartitionKey(user.registrationDate)
        val timestamp = Timestamp(Date().time)
        // TODO add as onStudying base phrases with count > X, for example 10 (add to properties)
        idList
            .chunked(BATCH_SIZE)
            .forEach {
                userPhraseRepository.addBaseFormsToVocabulary(user.id, it, partKey, onStudying, timestamp)
                userPhraseRepository.addRestFormsToVocabulary(user.id, it, partKey, timestamp)
            }

        userBookService.updateAllSummary(user, book)
    }

    fun setOnStudying(user: User, idList: List<Int>, onStudying: Boolean) {
        if (idList.isEmpty()) {
            return
        }

        idList
            .chunked(BATCH_SIZE)
            .forEach {
                userPhraseRepository.updateOnStudying(it, onStudying, user)
            }

        userBookService.updateAllSummary(user, null)
    }

    fun remove(user: User, idList: List<Int>) {
        if (idList.isEmpty()) {
            return
        }

        idList
            .chunked(BATCH_SIZE)
            .forEach {
                userPhraseRepository.deletePhrases(it, user)
            }

        userBookService.updateAllSummary(user, null)
    }
}
