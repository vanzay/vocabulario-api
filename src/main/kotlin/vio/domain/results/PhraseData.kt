package vio.domain.results

data class PhraseData(val base: BookPhraseEntry, val phrases: MutableList<BookPhraseEntry>) {

    fun getGroupFrequency(): Int? {
        return base.groupFrequency ?: phrases.find { it.groupFrequency != null }?.groupFrequency
    }
}
