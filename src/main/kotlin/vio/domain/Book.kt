package vio.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType
import java.util.*

@Entity
@Table(name = "book")
data class Book(
    @Id
    @SequenceGenerator(name = "book_gen", sequenceName = "book_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_gen")
    @Column(name = "book_id")
    var id: Int = 0,

    val author: String?,
    val title: String,
    val addedDate: Date = Date(),
    @JsonIgnore
    val contentHash: String,
    val coverUrl: String?,
    val contentUrl: String?,
    val audioContentUrl: String?,
    val totalWords: Long,
    val uniqueWords: Int,
    val uniqueGroups: Int,
    val common: Boolean = false,
    val translated: Boolean = false,

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    val language: Language
)
