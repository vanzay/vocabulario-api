package vio.domain

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface UserBookRepository : JpaRepository<UserBook, Int> {

    @EntityGraph(attributePaths = ["book", "book.language", "user"])
    fun findByUserAndBook(user: User, book: Book): UserBook?

    @EntityGraph(attributePaths = ["book", "book.language"])
    fun findAllByUser(user: User, pageable: Pageable): List<UserBook>

    fun countByUserAndAddedDateAfter(user: User, addedDate: Date): Long

    @Query(
        """
        SELECT ub
        FROM UserBook ub
        WHERE ub.user = :user
            AND (:language IS null OR ub.book.language = :language)
        ORDER BY ub.addedDate DESC
        """
    )
    fun getUserBooks(user: User, language: Language?): List<UserBook>
}
