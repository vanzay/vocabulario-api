package vio.conf

import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


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
        val keyBytes = Base64.getDecoder().decode(secretKey)
        return SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.jcaName)
    }
}
