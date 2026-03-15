package vio.domain

import jakarta.persistence.*

@Entity
@Table(name = "unexpected_word")
data class UnexpectedWord(
    @Id
    @SequenceGenerator(name = "unexpected_word_gen", sequenceName = "unexpected_word_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "unexpected_word_gen")
    @Column(name = "unexpected_word_id")
    var id: Int = 0,

    val term: String,
    val frequency: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    val book: Book
)
