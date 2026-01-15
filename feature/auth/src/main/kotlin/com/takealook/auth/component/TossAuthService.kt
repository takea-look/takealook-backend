package com.takealook.auth.component

import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.crypto.DirectDecrypter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64

@Service
class TossAuthService(
    @Value("\${toss.decryption-key}") private val decryptionKey: String,
    @Value("\${toss.aad}") private val aad: String
) {
    fun decryptToken(encryptedToken: String): String {
        try {
            val jweObject = JWEObject.parse(encryptedToken)
            
            val keyBytes = Base64.getDecoder().decode(decryptionKey)
            
            val decrypter = DirectDecrypter(keyBytes)
            
            jweObject.decrypt(decrypter)
            
            return jweObject.payload.toString()
        } catch (e: Exception) {
            throw RuntimeException("Failed to decrypt Toss token", e)
        }
    }
}
