package vio.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "user_book")
data class UserBook(
    @Id
    @SequenceGenerator(name = "user_book_gen", sequenceName = "user_book_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_book_gen")
    @Column(name = "user_book_id")
    var id: Int = 0,

    val addedDate: Date = Date(),
    val totalPhrases: Int = 0,
    val knownPhrases: Int = 0,
    val comfort: Short = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    val book: Book,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    val user: User
)
