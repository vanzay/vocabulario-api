package vio.domain

import org.springframework.data.jpa.repository.JpaRepository


interface BookPhraseRepository : JpaRepository<BookPhrase, Int>, BookPhraseRepositoryExt
