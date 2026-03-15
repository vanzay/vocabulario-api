package vio.utils

import java.util.*

enum class FileFormat {
    TEXT, EPUB, PDF;

    companion object {
        fun of(filename: String?, contentType: String?): FileFormat? {
            val extension = filename?.lowercase(Locale.getDefault())?.substringAfterLast('.', "")
            return when {
                "text/plain" == contentType || "application/octet-stream" == contentType && extension == "srt" -> TEXT
                "application/pdf" == contentType || "application/txt" == contentType && extension == "pdf" -> PDF
                "application/epub+zip" == contentType || "application/octet-stream" == contentType && extension == "epub" -> EPUB
                else -> null
            }
        }
    }
}
