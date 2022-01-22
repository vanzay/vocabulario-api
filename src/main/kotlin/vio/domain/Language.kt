package vio.domain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "language")
data class Language(
    @Id
    @Column(name = "language_id")
    val id: Int,

    @Column(unique = true)
    val nativeName: String,

    @Column(unique = true)
    val iso2: String,

    @Column(unique = true)
    val iso3: String,

    val coreSupported: Boolean,
    val uiSupported: Boolean
)
