package vio.processors

import vanzay.text.analyze.Vocabulary

class TxtProcessor(dictionaryIndicesPath: String) : BookProcessor(dictionaryIndicesPath) {

    override var title: String? = null
    override var vocabulary: Vocabulary? = null

    override fun process(data: ByteArray) {
        val detector = CharsetDetector(data)
        val text = String(data, detector.charset!!)
        super.process(text)
    }
}
