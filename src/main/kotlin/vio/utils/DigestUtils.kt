package vio.utils

import java.security.MessageDigest

object DigestUtils {

    fun sha256(data: ByteArray): String = getDigest(data, "SHA-256")

    fun sha256(text: String): String = getDigest(text.toByteArray(Charsets.UTF_8), "SHA-256")

    private fun getDigest(data: ByteArray, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        val hash: ByteArray = digest.digest(data)
        return toHex(hash)
    }

    private fun toHex(data: ByteArray): String {
        return data.joinToString("") { "%02x".format(it) }
    }
}
