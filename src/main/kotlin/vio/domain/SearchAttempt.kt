package vio.domain

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "search_attempt")
data class SearchAttempt(
    @Id
    @SequenceGenerator(name = "search_attempt_gen", sequenceName = "search_attempt_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "search_attempt_gen")
    @Column(name = "search_attempt_id")
    var id: Int = 0,

    val queryText: String,
    val addedDate: Date = Date()
)
