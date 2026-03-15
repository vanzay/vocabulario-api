package vio.processors

import vanzay.text.analyze.Dictionary
import vanzay.text.analyze.Vocabulary
import vanzay.text.language.LanguageDetector

abstract class BookProcessor(
    private val dictionaryIndicesPath: String
) {
    abstract var title: String?
    abstract var vocabulary: Vocabulary?

    abstract fun process(data: ByteArray)

    fun process(text: String) {
        val lang = LanguageDetector.detect(dictionaryIndicesPath, text)
        val dictionary = Dictionary.open(dictionaryIndicesPath, lang)
        vocabulary = Vocabulary(lang, dictionary)
        vocabulary!!.process(text)
        dictionary.close()
    }
}
