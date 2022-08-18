package vio.controllers

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*
import vio.controllers.requests.HandleAnswerRequest
import vio.controllers.responses.HandleAnswerResponse
import vio.domain.*
import vio.errors.ApiException
import vio.services.BookService
import vio.services.TrainingService
import vio.services.UserPhraseService
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping("/v1/training")
class TrainingController(
    private val languageRepository: LanguageRepository,
    private val phraseRepository: PhraseRepository,
    private val userPhraseRepository: UserPhraseRepository,
    private val bookService: BookService,
    private val trainingService: TrainingService,
    private val userPhraseService: UserPhraseService,
    @Value("\${training.phrases.per.lesson}")
    private val phrasesPerLesson: Int
) {

    @GetMapping("/phrases")
    fun getPhrases(mode: String, langIso2: String?, bookId: Int?, request: HttpServletRequest): List<UserPhrase> {
        val user = request.getAttribute("user") as User
        return getPhrases(user, mode, langIso2, bookId)
    }

    @PostMapping("/handle-answer")
    fun handleAnswer(
        @RequestBody handleAnswerRequest: HandleAnswerRequest,
        request: HttpServletRequest
    ): HandleAnswerResponse {
        val phrase = phraseRepository.findById(handleAnswerRequest.phraseId).orElse(null)
            ?: throw ApiException("PHRASE_NOT_FOUND")

        val user = request.getAttribute("user") as User
        val points =
            trainingService.calculatePoints(phrase.term, handleAnswerRequest.answer, handleAnswerRequest.hintsCount)
        trainingService.updatePoints(user, phrase, handleAnswerRequest.mode, points)
        return HandleAnswerResponse(points)
    }

    private fun getPhrases(
        user: User,
        mode: String,
        langIso2: String?,
        bookId: Int?
    ): List<UserPhrase> {
        val book = if (bookId == null) null else bookService.getBook(bookId, user)
        val language = book?.language ?: languageRepository.findByIso2(langIso2!!) ?: return emptyList()

        val sort = Sort.by(
//            Sort.Order.asc("${mode}Points"),
            Sort.Order.asc("lastActivity")
        )
        val pageRequest = PageRequest.of(0, phrasesPerLesson, sort)
        val userPhrases = userPhraseRepository.getPhrasesForTraining(user, book, language, pageRequest)
        userPhraseService.fillTranslations(userPhrases, user.language)
        return userPhrases.shuffled()
    }
}
