package vio.processors

import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import vanzay.text.analyze.Vocabulary

class PdfProcessor(dictionaryIndicesPath: String) : BookProcessor(dictionaryIndicesPath) {
    override var title: String? = null
    override var vocabulary: Vocabulary? = null

    override fun process(data: ByteArray) {
        val text = getText(data)
        super.process(text)
    }

    private fun getText(data: ByteArray): String {
        val content = StringBuilder()
        val reader = PdfReader(data)
        val numberOfPages = reader.numberOfPages
        for (i in 1..numberOfPages) {
            content.append(PdfTextExtractor.getTextFromPage(reader, i))
        }
        reader.close()
        return content.toString()
    }
}
