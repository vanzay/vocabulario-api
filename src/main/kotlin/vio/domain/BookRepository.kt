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
        WHERE b.common = true AND (:langIso2 IS null OR :langIso2 = '' OR b.language.iso2 = :langIso2)
        """
    )
    fun getBooks(langIso2: String?, pageable: Pageable): List<Book>

    @Query(
        value = "SELECT * FROM book WHERE make_tsvector(title, author) @@ to_tsquery('simple', :query) LIMIT :limit",
        nativeQuery = true
    )
    fun search(query: String, limit: Int): List<Book>

    @Modifying
    @Query(value = "CALL calculate_group_order(:bookId);", nativeQuery = true)
    fun calculateGroupOrder(bookId: Int)
}
