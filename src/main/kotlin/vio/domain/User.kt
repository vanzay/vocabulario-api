package vio.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "\"user\"")
data class User(
    @Id
    @SequenceGenerator(name = "user_gen", sequenceName = "user_id_seq", allocationSize = 1)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    val language: Language,
)
