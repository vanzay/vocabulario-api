package vio.controllers

import com.opencsv.CSVWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*
import vio.controllers.requests.*
import vio.controllers.responses.GetDictionaryInfoResponse
import vio.controllers.responses.GetPronunciationResponse
import vio.domain.*
import vio.domain.results.LanguageEntry
import vio.errors.ApiException
import vio.services.UserBookService
import vio.services.UserPhraseService
import java.io.StringWriter
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.random.Random.Default.nextInt

@RestController
@RequestMapping("/v1/dictionary")
class DictionaryController(
    private val bookRepository: BookRepository,
    private val languageRepository: LanguageRepository,
    private val pronunciationRepository: PronunciationRepository,
    private val userPhraseRepository: UserPhraseRepository,
    private val userBookService: UserBookService,
    private val userPhraseService: UserPhraseService,
    private val messageSource: MessageSource,
    @Value("\${dictionary.page.size}")
    private val pageSize: Int
) {

    @GetMapping("/languages")
    fun getLanguages(request: HttpServletRequest): List<LanguageEntry> {
        val user = request.getAttribute("user") as User
        return userPhraseRepository.getAvailableLanguages(user)
    }

    @GetMapping("/info")
    fun getInfo(langIso2: String, request: HttpServletRequest): GetDictionaryInfoResponse {
        val language = languageRepository.findByIso2(langIso2)
            ?: throw ApiException("UNSUPPORTED_LANGUAGE")
        val user = request.getAttribute("user") as User
        val summary = userPhraseRepository.getDictionarySummary(user, language)
        return GetDictionaryInfoResponse(summary, language)
    }

    @GetMapping("/phrases")
    fun getPhrases(
        langIso2: String,
        term: String?,
        onStudying: Boolean,
        page: Int,
        request: HttpServletRequest
    ): List<UserPhrase> {
        val user = request.getAttribute("user") as User
        val language = languageRepository.findByIso2(langIso2)
        return if (language == null) emptyList() else getPhrases(
            user,
            language,
            term,
            if (onStudying) onStudying else null,
            page
        )
    }

    @GetMapping("/pronunciation")
    fun getPronunciation(phraseId: Int): GetPronunciationResponse {
        val pronunciations = pronunciationRepository.findAllByPhraseId(phraseId)
        return GetPronunciationResponse(
            if (pronunciations.isEmpty()) null
            else if (pronunciations.size == 1) pronunciations[0].url
            else pronunciations[nextInt(0, pronunciations.size)].url
        )
    }

    @PostMapping("/save-translation")
    fun saveTranslation(@RequestBody saveTranslationRequest: SaveTranslationRequest, request: HttpServletRequest) {
        val user = request.getAttribute("user") as User
        userPhraseRepository.updateTranslation(
            user,
            saveTranslationRequest.userPhraseId,
            saveTranslationRequest.translation.take(64)
        )
    }

    @PostMapping("/add-phrases")
    fun addPhrases(@RequestBody addPhrasesRequest: AddPhrasesRequest, request: HttpServletRequest) {
        val user = request.getAttribute("user") as User
        val book = bookRepository.findById(addPhrasesRequest.bookId).orElse(null)
        if (book != null && book.common) {
            userBookService.attachBookToUser(book, user)
        }
        userPhraseService.add(user, book, addPhrasesRequest.idList, addPhrasesRequest.onStudying)
    }

    @PostMapping("/update-studying-status")
    fun updateStudyingStatus(
        @RequestBody updateStudyingStatusRequest: UpdateStudyingStatusRequest,
        request: HttpServletRequest
    ) {
        val user = request.getAttribute("user") as User
        userPhraseService.setOnStudying(
            user,
            updateStudyingStatusRequest.idList,
            updateStudyingStatusRequest.onStudying
        )
    }

    @PostMapping("/remove-phrases")
    fun removePhrases(@RequestBody removePhrasesRequest: RemovePhrasesRequest, request: HttpServletRequest) {
        val user = request.getAttribute("user") as User
        userPhraseService.remove(user, removePhrasesRequest.idList)
    }

    @PostMapping("/export-phrases")
    fun exportPhrases(
        @RequestBody exportPhrasesRequest: ExportPhrasesRequest,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val user = request.getAttribute("user") as User
        val userPhrases = userPhraseRepository.getExportData(user, exportPhrasesRequest.idList)
        userPhraseService.fillTranslations(userPhrases, user.language)
        val csv = buildCsv(userPhrases, Locale(user.language.iso2))

        response.setHeader("Content-disposition", "attachment; filename=dictionary.csv")
        response.contentType = "text/csv; charset=utf-8"
        response.writer.print(csv)
    }

    private fun buildCsv(userPhrases: List<UserPhrase>, locale: Locale): String {
        val writer = StringWriter()
        val csv = CSVWriter(writer)
        csv.writeNext(
            arrayOf(
                messageSource.getMessage("header.phrase.term", null, locale),
                messageSource.getMessage("header.phrase.translation", null, locale)
            )
        )
        userPhrases.forEach {
            csv.writeNext(arrayOf(it.phrase.term, it.translation))
        }
        csv.close()
        return writer.toString()
    }

    private fun getPhrases(
        user: User,
        language: Language,
        term: String?,
        onStudying: Boolean?,
        page: Int
    ): List<UserPhrase> {
        val sort = Sort.by(
            Sort.Order.desc("addedDate"),
            Sort.Order.asc("phrase.groupNumber"),
            Sort.Order.asc("phrase.term")
        )
        val pageRequest = PageRequest.of(page, pageSize, sort)
        val userPhrases = userPhraseRepository.getPhrases(
            user,
            language,
            if (term.isNullOrBlank()) null else "$term%",
            onStudying,
            pageRequest
        )
        userPhraseService.fillTranslations(userPhrases, user.language)
        return userPhrases
    }
}
