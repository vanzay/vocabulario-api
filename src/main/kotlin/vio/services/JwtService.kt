package vio.services

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Encoders
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import vio.domain.User
import vio.errors.InvalidTokenException
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
    private val secretKey: SecretKey,
    @Value("\${jwt.token.lifetime}")
    private val tokenLifetime: Long
) {

    fun issueToken(user: User): String {
        return Jwts.builder()
            .subject(user.email)
            .issuedAt(Date())
            .expiration(getExpirationTime())
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun validateToken(token: String): Claims {
        try {
            return parseToken(token)
        } catch (_: Exception) {
            throw InvalidTokenException()
        }
    }

    private fun getExpirationTime(): Date {
        val date = Instant.now().plusSeconds(tokenLifetime)
        return Date.from(date)
    }

    private fun parseToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun generateHmacSecretKey(): String {
        val key = Jwts.SIG.HS256.key().build()
        return Encoders.BASE64.encode(key.encoded)
    }

//    fun generateRSAKeys(): Pair<String, String> {
//        val keyPair = Jwts.SIG.RS256.keyPair().build()
//        val secretString = Encoders.BASE64.encode(keyPair.private.encoded)
//        val publicString = Encoders.BASE64.encode(keyPair.public.encoded)
//        return Pair(secretString, publicString)
//    }
}
