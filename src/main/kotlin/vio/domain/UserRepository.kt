package vio.domain

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Int> {

    @EntityGraph(attributePaths = ["language"])
    fun findByEmail(email: String): User?

    @Modifying
    @Query(
        value = """
        UPDATE "user" SET dicts = jsonb_set(dicts, concat('{', :dictionary, '}')::text[], 'true')
        WHERE user_id = :userId
        """, nativeQuery = true
    )
    fun addDictionary(userId: Int, dictionary: String)
}
