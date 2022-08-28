package vio.domain.results

data class BookPhraseEntry(
    val frequency: Int?,
    val groupFrequency: Int?,
    val groupOrder: Int?,
    val phraseId: Int,
    val baseForm: Boolean?,
    val basePhraseId: Int?,
    val term: String?,
    val groupNumber: Int?,
    val onStudying: Boolean?,
    val userTranslation: String?,
    val translation: String?
)
