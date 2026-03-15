package vio.processors

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import org.jsoup.select.NodeTraversor
import vanzay.text.analyze.Vocabulary
import java.io.ByteArrayInputStream

class EpubProcessor(dictionaryIndicesPath: String) : BookProcessor(dictionaryIndicesPath) {
    override var title: String? = null
    override var vocabulary: Vocabulary? = null

    override fun process(data: ByteArray) {
        val epubReader = EpubReader()
        val book = epubReader.readEpub(ByteArrayInputStream(data))
        title = book.title
//        book.getMetadata().getAuthors()
//        book.getMetadata().getLanguage()
        val text = getText(book)
        super.process(text)
    }

    private fun getText(book: Book): String {
        val content = StringBuilder()
        val resourceList = book.contents
        for (res in resourceList) {
            content.append(processResource(res))
        }
        return content.toString()
    }

    private fun processResource(res: Resource): String {
        val doc = Jsoup.parse(res.inputStream, res.inputEncoding, "")
        val formatter = HtmlToTextFormatter()
        NodeTraversor.traverse(formatter, doc.body())
        return formatter.toString()
    }
}
