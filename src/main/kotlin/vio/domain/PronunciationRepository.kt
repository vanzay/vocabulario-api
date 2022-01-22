package vio.domain

import org.springframework.data.jpa.repository.JpaRepository

interface PronunciationRepository : JpaRepository<Pronunciation, Int> {

    fun findAllByPhraseId(phraseId: Int): List<Pronunciation>
}
