package vio.domain

import jakarta.persistence.*
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType

@Entity
@Table(name = "translation")
data class Translation(
    @Id
    @SequenceGenerator(name = "translation_gen", sequenceName = "translation_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "translation_gen")
    @Column(name = "translation_id")
    var id: Int = 0,

    val term: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phrase_id")
    val phrase: Phrase,

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    val language: Language
)
