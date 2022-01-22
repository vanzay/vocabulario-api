package vio.services

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Encoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import vio.domain.User
import vio.errors.InvalidTokenException
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
    private val secretKey: SecretKey,
    @Value("\${jwt.token.lifetime}")
    private val tokenLifetime: Int
) {

    fun issueToken(user: User): String {
        return Jwts.builder()
            .setSubject(user.email)
            .setIssuedAt(Date())
            .setExpiration(getExpirationTime())
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun validateToken(token: String): Claims {
        try {
            return parseToken(token)
        } catch (ex: Exception) {
            throw InvalidTokenException()
        }
    }

    private fun getExpirationTime(): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.SECOND, tokenLifetime)
        return cal.time
    }

    private fun parseToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    }

    fun generateHmacSecretKey(): String {
        val key = Keys.secretKeyFor(SignatureAlgorithm.HS256)
        return Encoders.BASE64.encode(key.encoded)
    }

//    fun generateRSAKeys(): Pair<String, String> {
//        val keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256)
//        val secretString = Encoders.BASE64.encode(keyPair.private.encoded)
//        val publicString = Encoders.BASE64.encode(keyPair.public.encoded)
//        return Pair(secretString, publicString)
//    }
}
