package com.takealook.auth.config

import okhttp3.OkHttpClient
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import java.io.FileReader
import java.security.KeyStore
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit

@Configuration
class TossApiConfig(
    @Value("\${toss.api.base-url:https://apps-in-toss-api.toss.im}")
    private val baseUrl: String,

    @Value("\${toss.api.cert-path:}")
    private val certPath: String,

    @Value("\${toss.api.key-path:}")
    private val keyPath: String
) {

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    @Bean
    fun tossHttpClient(): OkHttpClient {
        return if (certPath.isNotEmpty() && keyPath.isNotEmpty()) {
            val certFile = File(certPath)
            val keyFile = File(keyPath)

            if (!certFile.exists()) {
                throw IllegalArgumentException("Toss cert file not found: $certPath")
            }
            if (!keyFile.exists()) {
                throw IllegalArgumentException("Toss key file not found: $keyPath")
            }

            println("INFO: Toss mTLS configured with cert=$certPath, key=$keyPath")

            val (sslContext, trustManager) = createSSLContext(certFile, keyFile)

            OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .sslSocketFactory(sslContext.socketFactory, trustManager)
                .build()
        } else {
            println("WARNING: mTLS certificates not configured. Toss API calls will fail in production.")
            println("Set toss.api.cert-path and toss.api.key-path in application.properties")
            OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        }
    }

    private fun createSSLContext(certFile: File, keyFile: File): Pair<javax.net.ssl.SSLContext, javax.net.ssl.X509TrustManager> {
        val certFactory = CertificateFactory.getInstance("X.509")
        val certificate = certFactory.generateCertificate(java.io.FileInputStream(certFile)) as X509Certificate

        val privateKey = readPrivateKey(keyFile)

        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(null, null)
        keyStore.setKeyEntry(
            "client",
            privateKey,
            "".toCharArray(),
            arrayOf(certificate)
        )

        val kmf = javax.net.ssl.KeyManagerFactory.getInstance(
            javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm()
        )
        kmf.init(keyStore, "".toCharArray())

        val tmf = javax.net.ssl.TrustManagerFactory.getInstance(
            javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm()
        )
        tmf.init(null as java.security.KeyStore?)

        val trustManager = tmf.trustManagers.first { it is javax.net.ssl.X509TrustManager } as javax.net.ssl.X509TrustManager

        val sslContext = javax.net.ssl.SSLContext.getInstance("TLS")
        sslContext.init(kmf.keyManagers, tmf.trustManagers, null)

        return Pair(sslContext, trustManager)
    }

    private fun readPrivateKey(keyFile: File): java.security.PrivateKey {
        FileReader(keyFile).use { reader ->
            val pemParser = PEMParser(reader)
            val privateKeyObject = pemParser.readObject()

            return when (privateKeyObject) {
                is org.bouncycastle.asn1.pkcs.PrivateKeyInfo -> {
                    val converter = JcaPEMKeyConverter().setProvider("BC")
                    converter.getPrivateKey(privateKeyObject)
                }
                is org.bouncycastle.openssl.PEMKeyPair -> {
                    val converter = JcaPEMKeyConverter().setProvider("BC")
                    converter.getKeyPair(privateKeyObject).private
                }
                else -> {
                    throw IllegalArgumentException("Unsupported private key format: ${privateKeyObject::class.simpleName}")
                }
            }
        }
    }

    @Bean
    fun tossApiBaseUrl(): String = baseUrl
}
