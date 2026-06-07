package tech.strxmlpipeline.infrastructure.security.jwt

import org.springframework.stereotype.Component
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Component
class JwtKeyLoader(private val props: JwtProperties) {

    fun privateKey(): ECPrivateKey {
        val pem = File(props.privateKeyPath).readText()
            .replace("-----BEGIN EC PRIVATE KEY-----", "")
            .replace("-----END EC PRIVATE KEY-----", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
        val keyBytes = Base64.getDecoder().decode(pem)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("EC").generatePrivate(keySpec) as ECPrivateKey
    }

    fun publicKey(): ECPublicKey {
        val pem = File(props.publicKeyPath).readText()
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
        val keyBytes = Base64.getDecoder().decode(pem)
        val keySpec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("EC").generatePublic(keySpec) as ECPublicKey
    }
}