package vio.domain.results

class DictionarySummary(
    val knownPhrases: Long,
    val unknownPhrases: Long
) {
    fun getTotalPhrases(): Long = knownPhrases + unknownPhrases
}
