package vio.conf

import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey


@Configuration
class JwtConfig(
    @Value("\${jwt.secret.key}")
    private val secretKey: String
) {

//    @Bean
//    fun jwtPrivateKey(): PrivateKey {
//        val keyFactory = KeyFactory.getInstance("RSA")
//        val keyBytes = Base64.getDecoder().decode(privateKey)
//        val keySpec = PKCS8EncodedKeySpec(keyBytes)
//        return keyFactory.generatePrivate(keySpec)
//    }
//
//    @Bean
//    fun jwtPublicKey(): PublicKey {
//        val keyFactory = KeyFactory.getInstance("RSA")
//        val keyBytes = Base64.getDecoder().decode(publicKey)
//        val publicKeySpec = X509EncodedKeySpec(keyBytes)
//        return keyFactory.generatePublic(publicKeySpec)
//    }

    @Bean
    fun jwtSecretKey(): SecretKey {
        val keyBytes = secretKey.toByteArray(StandardCharsets.UTF_8)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}
