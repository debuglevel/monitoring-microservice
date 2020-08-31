package de.debuglevel.monitoring.monitors

import mu.KotlinLogging
import java.net.HttpURLConnection
import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

object SslTrustModifier {
    private val logger = KotlinLogging.logger {}

    private val TRUSTING_HOSTNAME_VERIFIER = TrustingHostnameVerifier()
    private var factory: SSLSocketFactory? = null

    /** Call this with any HttpURLConnection, and it will
     * modify the trust settings if it is an HTTPS connection.  */
    @Throws(KeyManagementException::class, NoSuchAlgorithmException::class, KeyStoreException::class)
    fun relaxHostChecking(urlConnection: HttpURLConnection) {
        logger.debug { "Relaxing host checking..." }

        if (urlConnection is HttpsURLConnection) {
            val factory = prepareFactory(urlConnection)
            urlConnection.sslSocketFactory = factory!!
            urlConnection.hostnameVerifier = TRUSTING_HOSTNAME_VERIFIER
        }

        logger.debug { "Relaxed host checking" }
    }

    @Synchronized
    @Throws(NoSuchAlgorithmException::class, KeyStoreException::class, KeyManagementException::class)
    internal fun prepareFactory(httpsConnection: HttpsURLConnection): SSLSocketFactory? {
        logger.debug { "Preparing factory..." }
        if (factory == null) {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(AlwaysTrustManager()), null)
            factory = sslContext.socketFactory
        }
        logger.debug { "Prepared factory: $factory" }
        return factory
    }

    private class TrustingHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession): Boolean {
            return true
        }
    }

    private class AlwaysTrustManager : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(arg0: Array<X509Certificate>, arg1: String) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(arg0: Array<X509Certificate>, arg1: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate>? {
            return null
        }
    }
}