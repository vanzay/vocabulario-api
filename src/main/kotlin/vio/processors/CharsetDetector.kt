package vio.processors

import java.nio.charset.Charset

/**
 * Ported from groovy.util.CharsetToolkit
 */
class CharsetDetector(private val buffer: ByteArray) {
    private var defaultCharset = Charset.forName("UTF-8")
    var charset: Charset? = null
        /**
         * Gets the enforce8Bit flag, in case we do not want to ever get a US-ASCII encoding.
         *
         * @return a boolean representing the flag of use of US-ASCII.
         */
        get() {
            if (field == null) field = guessEncoding()
            return field
        }
        private set
    /**
     * If US-ASCII is recognized, enforce to return the default encoding, rather than US-ASCII.
     * It might be a file without any special character in the range 128-255, but that may be or become
     * a file encoded with the default `charset` rather than US-ASCII.
     *
     * @param enforce a boolean specifying the use or not of US-ASCII.
     */
    var enforce8Bit = true

    /**
     * Defines the default `Charset` used in case the buffer represents
     * an 8-bit `Charset`.
     *
     * @param defaultCharset the default `Charset` to be returned
     * if an 8-bit `Charset` is encountered.
     */
    fun setDefaultCharset(defaultCharset: Charset?) {
        if (defaultCharset != null) this.defaultCharset = defaultCharset else this.defaultCharset = defaultSystemCharset
    }

    /**
     * Retrieves the default Charset
     */
    fun getDefaultCharset(): Charset {
        return defaultCharset
    }

    /**
     * Guess the encoding of the provided buffer.
     * If Byte Order Markers are encountered at the beginning of the buffer, we immediately
     * return the charset implied by this BOM. Otherwise, the file would not be a human
     * readable text file.
     *
     *
     * If there is no BOM, this method tries to discern whether the file is UTF-8 or not.
     * If it is not UTF-8, we assume the encoding is the default system encoding
     * (of course, it might be any 8-bit charset, but usually, an 8-bit charset is the default one).
     *
     *
     * It is possible to discern UTF-8 thanks to the pattern of characters with a multi-byte sequence.
     * <pre>
     * UCS-4 range (hex.)        UTF-8 octet sequence (binary)
     * 0000 0000-0000 007F       0xxxxxxx
     * 0000 0080-0000 07FF       110xxxxx 10xxxxxx
     * 0000 0800-0000 FFFF       1110xxxx 10xxxxxx 10xxxxxx
     * 0001 0000-001F FFFF       11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
     * 0020 0000-03FF FFFF       111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
     * 0400 0000-7FFF FFFF       1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
    </pre> *
     * With UTF-8, 0xFE and 0xFF never appear.
     *
     * @return the Charset recognized.
     */
    private fun guessEncoding(): Charset {
        // if the file has a Byte Order Marker, we can assume the file is in UTF-xx
        // otherwise, the file would not be human readable
        if (hasUTF8Bom()) return Charset.forName("UTF-8")
        if (hasUTF16LEBom()) return Charset.forName("UTF-16LE")
        if (hasUTF16BEBom()) return Charset.forName("UTF-16BE")

        // if a byte has its most significant bit set, the file is in UTF-8 or in the default encoding
        // otherwise, the file is in US-ASCII
        var highOrderBit = false

        // if the file is in UTF-8, high order bytes must have a certain value, in order to be valid
        // if it's not the case, we can assume the encoding is the default encoding of the system
        var validU8Char = true

        // TODO the buffer is not read up to the end, but up to length - 6
        val length = buffer.size
        var i = 0
        while (i < length - 6) {
            val b0 = buffer[i]
            val b1 = buffer[i + 1]
            val b2 = buffer[i + 2]
            val b3 = buffer[i + 3]
            val b4 = buffer[i + 4]
            val b5 = buffer[i + 5]
            if (b0 < 0) {
                // a high order bit was encountered, thus the encoding is not US-ASCII
                // it may be either an 8-bit encoding or UTF-8
                highOrderBit = true
                // a two-bytes sequence was encountered
                if (isTwoBytesSequence(b0)) {
                    // there must be one continuation byte of the form 10xxxxxx,
                    // otherwise the following character is is not a valid UTF-8 construct
                    if (!isContinuationChar(b1)) validU8Char = false else i++
                } else if (isThreeBytesSequence(b0)) {
                    // there must be two continuation bytes of the form 10xxxxxx,
                    // otherwise the following character is is not a valid UTF-8 construct
                    if (!(isContinuationChar(b1) && isContinuationChar(b2))) validU8Char = false else i += 2
                } else if (isFourBytesSequence(b0)) {
                    // there must be three continuation bytes of the form 10xxxxxx,
                    // otherwise the following character is is not a valid UTF-8 construct
                    if (!(isContinuationChar(b1) && isContinuationChar(b2) && isContinuationChar(b3))) validU8Char =
                        false else i += 3
                } else if (isFiveBytesSequence(b0)) {
                    // there must be four continuation bytes of the form 10xxxxxx,
                    // otherwise the following character is is not a valid UTF-8 construct
                    if (!(isContinuationChar(b1)
                                && isContinuationChar(b2)
                                && isContinuationChar(b3)
                                && isContinuationChar(b4))
                    ) validU8Char = false else i += 4
                } else if (isSixBytesSequence(b0)) {
                    // there must be five continuation bytes of the form 10xxxxxx,
                    // otherwise the following character is is not a valid UTF-8 construct
                    if (!(isContinuationChar(b1)
                                && isContinuationChar(b2)
                                && isContinuationChar(b3)
                                && isContinuationChar(b4)
                                && isContinuationChar(b5))
                    ) validU8Char = false else i += 5
                } else validU8Char = false
            }
            if (!validU8Char) break
            i++
        }
        // if no byte with an high order bit set, the encoding is US-ASCII
        // (it might have been UTF-7, but this encoding is usually internally used only by mail systems)
        if (!highOrderBit) {
            // returns the default charset rather than US-ASCII if the enforce8Bit flag is set.
            return if (enforce8Bit) defaultCharset else Charset.forName("US-ASCII")
        }
        // if no invalid UTF-8 were encountered, we can assume the encoding is UTF-8,
        // otherwise the file would not be human readable
        return if (validU8Char) Charset.forName("UTF-8") else defaultCharset
        // finally, if it's not UTF-8 nor US-ASCII, let's assume the encoding is the default encoding
    }

    /**
     * Has a Byte Order Marker for UTF-8 (Used by Microsoft's Notepad and other editors).
     *
     * @return true if the buffer has a BOM for UTF8.
     */
    fun hasUTF8Bom(): Boolean {
        return if (buffer.size >= 3) buffer[0].toInt() == -17 && buffer[1].toInt() == -69 && buffer[2].toInt() == -65 else false
    }

    /**
     * Has a Byte Order Marker for UTF-16 Low Endian
     * (ucs-2le, ucs-4le, and ucs-16le).
     *
     * @return true if the buffer has a BOM for UTF-16 Low Endian.
     */
    fun hasUTF16LEBom(): Boolean {
        return if (buffer.size >= 2) buffer[0].toInt() == -1 && buffer[1].toInt() == -2 else false
    }

    /**
     * Has a Byte Order Marker for UTF-16 Big Endian
     * (utf-16 and ucs-2).
     *
     * @return true if the buffer has a BOM for UTF-16 Big Endian.
     */
    fun hasUTF16BEBom(): Boolean {
        return if (buffer.size >= 2) buffer[0].toInt() == -2 && buffer[1].toInt() == -1 else false
    }

    companion object {
        /**
         * If the byte has the form 10xxxxx, then it's a continuation byte of a multiple byte character;
         *
         * @param b a byte.
         * @return true if it's a continuation char.
         */
        private fun isContinuationChar(b: Byte): Boolean {
            return -128 <= b && b <= -65
        }

        /**
         * If the byte has the form 110xxxx, then it's the first byte of a two-bytes sequence character.
         *
         * @param b a byte.
         * @return true if it's the first byte of a two-bytes sequence.
         */
        private fun isTwoBytesSequence(b: Byte): Boolean {
            return -64 <= b && b <= -33
        }

        /**
         * If the byte has the form 1110xxx, then it's the first byte of a three-bytes sequence character.
         *
         * @param b a byte.
         * @return true if it's the first byte of a three-bytes sequence.
         */
        private fun isThreeBytesSequence(b: Byte): Boolean {
            return -32 <= b && b <= -17
        }

        /**
         * If the byte has the form 11110xx, then it's the first byte of a four-bytes sequence character.
         *
         * @param b a byte.
         * @return true if it's the first byte of a four-bytes sequence.
         */
        private fun isFourBytesSequence(b: Byte): Boolean {
            return -16 <= b && b <= -9
        }

        /**
         * If the byte has the form 11110xx, then it's the first byte of a five-bytes sequence character.
         *
         * @param b a byte.
         * @return true if it's the first byte of a five-bytes sequence.
         */
        private fun isFiveBytesSequence(b: Byte): Boolean {
            return -8 <= b && b <= -5
        }

        /**
         * If the byte has the form 1110xxx, then it's the first byte of a six-bytes sequence character.
         *
         * @param b a byte.
         * @return true if it's the first byte of a six-bytes sequence.
         */
        private fun isSixBytesSequence(b: Byte): Boolean {
            return -4 <= b && b <= -3
        }

        /**
         * Retrieve the default charset of the system.
         *
         * @return the default `Charset`.
         */
        val defaultSystemCharset: Charset
            get() = Charset.forName(System.getProperty("file.encoding"))
    }
}
