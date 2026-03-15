package vio.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType
import org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonbType
import java.util.*

@Entity
@Table(name = "\"user\"")
data class User(
    @Id
    @SequenceGenerator(name = "user_gen", sequenceName = "user_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_gen")
    @Column(name = "user_id")
    var id: Int = 0,

    @Column(unique = true)
    val email: String,

    @JsonIgnore
    val confirmed: Boolean = false,

    @JsonIgnore
    val password: String,
    @JsonIgnore
    val registrationDate: Date = Date(),

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    val language: Language,

    @JdbcType(PostgreSQLJsonPGObjectJsonbType::class)
    val dicts: String = "{}",
)
