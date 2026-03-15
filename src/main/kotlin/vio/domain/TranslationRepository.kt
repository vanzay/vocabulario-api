package vio.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TranslationRepository : JpaRepository<Translation, Int> {

    @Query("SELECT t FROM Translation t WHERE t.phrase.id IN (:idList) AND t.language = :language")
    fun getTranslations(idList: List<Int>, language: Language): List<Translation>
}
