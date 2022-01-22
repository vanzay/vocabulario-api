package vio.domain

import javax.persistence.*

@Entity
@Table(name = "translation")
data class Translation(
    @Id
    @SequenceGenerator(name = "translation_gen", sequenceName = "translation_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "translation_gen")
    @Column(name = "translation_id")
    var id: Int = 0,

    val term: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phrase_id")
    val phrase: Phrase,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    val language: Language
)
