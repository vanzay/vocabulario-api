package vio.domain

import org.springframework.data.jpa.repository.JpaRepository

interface LanguageRepository : JpaRepository<Language, Int> {

    fun findByIso2(iso2: String): Language?

    fun findByIso3(iso3: String): Language?
}
