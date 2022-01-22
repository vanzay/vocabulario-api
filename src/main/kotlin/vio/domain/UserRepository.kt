package vio.domain

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Int> {

    @EntityGraph(attributePaths = ["language"])
    fun findByEmail(email: String): User?
}
