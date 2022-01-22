package vio.domain

import vio.domain.results.PhraseData

interface BookPhraseRepositoryExt {

    fun getPhrases(user: User?, language: Language, book: Book, inDictionary: Boolean, page: Int, size: Int): Collection<PhraseData>
}
