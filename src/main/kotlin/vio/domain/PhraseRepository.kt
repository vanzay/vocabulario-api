package vio.domain

import org.springframework.data.jpa.repository.JpaRepository

interface PhraseRepository : JpaRepository<Phrase, Int>
