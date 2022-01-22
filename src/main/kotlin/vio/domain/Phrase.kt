package vio.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*

@Entity
@Table(name = "phrase")
data class Phrase(
    @Id
    @SequenceGenerator(name = "phrase_gen", sequenceName = "phrase_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "phrase_gen")
    @Column(name = "phrase_id")
    var id: Int = 0,

    val term: String,
    val transcription: String?,
    val groupNumber: Int = 0,
    val visible: Boolean = true,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_phrase_id")
    @JsonIgnore
    val basePhrase: Phrase?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    @JsonIgnore
    val language: Language
) {

    fun isBaseForm() = basePhrase == null
}
