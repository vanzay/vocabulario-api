package vio.services

import org.apache.commons.text.similarity.LevenshteinDistance
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import vio.domain.Phrase
import vio.domain.User
import vio.domain.UserPhraseRepository
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.math.ceil

@Service
@Transactional
class TrainingService(
    private val userPhraseRepository: UserPhraseRepository,
    @Value("\${training.letters.per.error}")
    private val lettersPerError: Double,
    @Value("\${training.max.answer.points}")
    private val maxAnswerPoints: Int,
    @Value("\${training.complete.progress.points}")
    private val completeProgressPoints: Int
) {

    fun calculatePoints(term: String?, answer: String, hints: Int): Int {
        if (term != null && answer.isNotBlank()) {
            if (term == answer) {
                return if (hints > maxAnswerPoints) 0 else maxAnswerPoints - hints
            } else {
                val maxErrors = ceil(term.length / lettersPerError)
                if (LevenshteinDistance().apply(term, answer) > maxErrors) {
                    return -maxAnswerPoints
                }
            }
        }
        return 0
    }

    fun updatePoints(user: User, phrase: Phrase, mode: String, points: Int) {
        val userPhrase = userPhraseRepository.findByUserAndPhrase(user, phrase)
        if (userPhrase == null) {
            return
        }

        val updatedUserPhrase = if (points != 0) {
            val auditionPoints =
                if (mode == "audition") getFixedPoints(userPhrase.auditionPoints + points) else userPhrase.auditionPoints
            val memoryPoints =
                if (mode == "memory") getFixedPoints(userPhrase.memoryPoints + points) else userPhrase.memoryPoints
            userPhrase.copy(
                auditionPoints = auditionPoints,
                memoryPoints = memoryPoints,
                onStudying = memoryPoints + auditionPoints < completeProgressPoints * 2,
                lastActivity = Date()
            )
        } else {
            userPhrase.copy(
                lastActivity = Date()
            )
        }

        userPhraseRepository.save(updatedUserPhrase)
    }

    fun refreshPoints() {
        val limitDate = getRefreshLimitDate()
        userPhraseRepository.refreshMemoryPoints(maxAnswerPoints, limitDate)
        userPhraseRepository.refreshAuditionPoints(maxAnswerPoints, limitDate)
    }

    private fun getFixedPoints(points: Int): Int {
        return points.coerceIn(0..completeProgressPoints)
    }

    private fun getRefreshLimitDate(): Date {
        val date = Instant.now().minus(Duration.ofDays(30))
        return Date.from(date)
    }
}
