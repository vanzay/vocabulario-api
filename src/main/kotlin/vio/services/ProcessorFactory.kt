package vio.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import vio.processors.BookProcessor
import vio.processors.EpubProcessor
import vio.processors.PdfProcessor
import vio.processors.TxtProcessor
import vio.utils.FileFormat

@Service
class ProcessorFactory(
    @Value("\${dictionary.indices.path}")
    private val dictionaryIndicesPath: String
) {

    fun getProcessor(fileFormat: FileFormat): BookProcessor {
        return when (fileFormat) {
            FileFormat.TEXT -> TxtProcessor(dictionaryIndicesPath)
            FileFormat.EPUB -> EpubProcessor(dictionaryIndicesPath)
            FileFormat.PDF -> PdfProcessor(dictionaryIndicesPath)
        }
    }
}
