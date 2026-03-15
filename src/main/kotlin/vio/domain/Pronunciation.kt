package vio.domain

import jakarta.persistence.*

@Entity
@Table(name = "pronunciation")
data class Pronunciation(
    @Id
    @SequenceGenerator(name = "pronunciation_gen", sequenceName = "pronunciation_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pronunciation_gen")
    @Column(name = "pronunciation_id")
    var id: Int = 0,

    val url: String,
    val sortOrder: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phrase_id")
    val phrase: Phrase
)
