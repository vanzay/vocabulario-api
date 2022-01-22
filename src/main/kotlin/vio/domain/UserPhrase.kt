package vio.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "user_phrase")
data class UserPhrase(
    @Id
    @Column(name = "user_phrase_id")
    val id: Int,

    @JsonIgnore
    val partKey: Int,
    val addedDate: Date = Date(),
    val onStudying: Boolean,
    val lastActivity: Date = Date(),
    val memoryPoints: Int = 0,
    val auditionPoints: Int = 0,
    var translation: String?,
    val association: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phrase_id")
    val phrase: Phrase
) {

    companion object {
        // TODO use training.complete.progress.points property
        const val COMPLETE_PROGRESS_POINTS = 30
    }

    fun getMemoryProgress() = (memoryPoints * 100) / COMPLETE_PROGRESS_POINTS
    fun getAuditionProgress() = (auditionPoints * 100) / COMPLETE_PROGRESS_POINTS
}
