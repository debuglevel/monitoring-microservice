package de.debuglevel.monitoring.monitors

import de.debuglevel.monitoring.ServiceState
import de.debuglevel.monitoring.monitoring.Monitoring
import mu.KotlinLogging
import java.net.URI

interface Monitor {
    /**
     * Check a monitoring concerning its serviceState
     */
    fun check(monitoring: Monitoring): ServiceState

    /**
     * Whether a monitoring has a valid format
     */
    fun isValid(url: String): Boolean

    companion object {
        private val logger = KotlinLogging.logger {}

        /**
         * Gets the appropriate monitor for an URL string
         */
        fun get(url: String): Monitor {
            logger.debug { "Getting monitor for $url..." }
            val uri = try {
                URI(url)
            } catch (e: Exception) {
                // e.g. java.net.URISyntaxException: Expected scheme name at index 0: ://
                throw InvalidMonitoringFormatException(url, e)
            }

            val monitor = when (uri.scheme) {
                "http" -> HttpMonitor()
                "https" -> HttpMonitor()
                "tcp" -> TcpMonitor()
                "icmp" -> IcmpMonitor()
                else -> throw UnsupportedMonitoringProtocolException(uri.scheme)
            }

            logger.debug { "Got monitor for $url: $monitor" }
            return monitor
        }
    }

    class UnsupportedMonitoringProtocolException(val scheme: String) : Exception("Protocol '$scheme' is not supported.")
    class InvalidMonitoringFormatException(url: String, inner: Exception) :
        Exception("Monitoring with URL '$url' has an invalid format: ${inner.message}")
}