package vio.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType

@Entity
@Table(name = "phrase")
data class Phrase(
    @Id
    @SequenceGenerator(name = "phrase_gen", sequenceName = "phrase_id_seq")
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

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    @JsonIgnore
    val language: Language
) {

    fun getBaseForm() = basePhrase == null
}
