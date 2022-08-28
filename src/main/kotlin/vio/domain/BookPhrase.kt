package vio.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import vio.domain.results.BookPhraseEntry
import javax.persistence.*

@Entity
@Table(name = "book_phrase")
@SqlResultSetMapping(
    name = "BookPhraseMapping",
    classes = [
        ConstructorResult(
            targetClass = BookPhraseEntry::class,
            columns = arrayOf(
                ColumnResult(name = "frequency", type = Int::class),
                ColumnResult(name = "groupFrequency", type = Int::class),
                ColumnResult(name = "groupOrder", type = Int::class),
                ColumnResult(name = "phraseId", type = Int::class),
                ColumnResult(name = "baseForm", type = Boolean::class),
                ColumnResult(name = "basePhraseId", type = Int::class),
                ColumnResult(name = "term"),
                ColumnResult(name = "groupNumber", type = Int::class),
                ColumnResult(name = "onStudying", type = Boolean::class),
                ColumnResult(name = "userTranslation"),
                ColumnResult(name = "translation")
            )
        )
    ]
)
data class BookPhrase(
    @Id
    @SequenceGenerator(name = "book_phrase_gen", sequenceName = "book_phrase_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_phrase_gen")
    @Column(name = "book_phrase_id")
    var id: Long = 0,

    @JsonIgnore
    val partKey: Int,
    val frequency: Int,
    val groupFrequency: Int = 0,
    val groupOrder: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    val book: Book,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phrase_id")
    val phrase: Phrase
)
