package vio.domain

import vio.domain.results.PhraseData

interface BookPhraseRepositoryExt {

    fun getPhrases(user: User?, language: Language, book: Book, inDictionary: Boolean, offset: Int, size: Int): Collection<PhraseData>
}
