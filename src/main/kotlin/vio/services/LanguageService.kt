package vio.services

import org.springframework.stereotype.Service
import vio.domain.Language
import vio.domain.LanguageRepository

@Service
class LanguageService(
    private val languageRepository: LanguageRepository
) {

    fun getOrDefault(langIso2: String?): Language {
        val language = if (langIso2.isNullOrBlank()) null else languageRepository.findByIso2(langIso2)
        return language ?: languageRepository.findByIso2("en")!!
    }
}
