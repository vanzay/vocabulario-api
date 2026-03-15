package vio.domain

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface BookRepository : JpaRepository<Book, Int> {

    fun findByContentHash(contentHash: String): Book?

    @Query(
        """
        SELECT b
        FROM Book b
        WHERE b.common = true AND (:#{#language == null} = TRUE OR b.language = :language)
        """
    )
    fun getBooks(language: Language?, pageable: Pageable): List<Book>

    @Query(
        value = """
        SELECT *
        FROM book
        WHERE common = true AND make_tsvector(title, author) @@ to_tsquery('simple', :query)
        LIMIT :limit
        """,
        nativeQuery = true
    )
    fun search(query: String, limit: Int): List<Book>

    @Modifying
    @Query(value = "CALL calculate_group_order(:bookId);", nativeQuery = true)
    fun calculateGroupOrder(bookId: Int)
}
