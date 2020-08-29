package de.debuglevel.monitoring.monitors

import de.debuglevel.monitoring.ServiceState
import de.debuglevel.monitoring.monitoring.Monitoring
import java.net.URI

interface Monitor {
    /**
     * Check a monitoring concerning its serviceState
     */
    fun check(monitoring: Monitoring): ServiceState

    /**
     * Whether a monitoring has a valid format
     */
    fun isValid(urlString: String): Boolean

    companion object {
        /**
         * Gets the appropriate monitor for an URL string
         */
        fun get(url: String) = get(URI(url))

        /**
         * Gets the appropriate monitor for an URI
         */
        fun get(uri: URI): Monitor {
            return when (uri.scheme) {
                "http" -> HttpMonitor()
                "https" -> HttpMonitor()
                "tcp" -> TcpMonitor()
                "icmp" -> IcmpMonitor()
                "" -> throw EmptyMonitoringProtocolException()
                null -> throw EmptyMonitoringProtocolException()
                else -> throw UnsupportedMonitoringProtocolException(uri.scheme)
            }
        }
    }

    class EmptyMonitoringProtocolException : Exception("Protocol must not be empty.")
    class UnsupportedMonitoringProtocolException(val scheme: String) : Exception("Protocol '$scheme' is not supported.")
}