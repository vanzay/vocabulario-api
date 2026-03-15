package vio.domain

import org.springframework.data.jpa.repository.JpaRepository

interface UnexpectedWordRepository : JpaRepository<UnexpectedWord, Int>
