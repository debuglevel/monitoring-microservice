package de.debuglevel.monitoring.monitors

import de.debuglevel.monitoring.Monitoring
import de.debuglevel.monitoring.ServiceState

interface Monitor {
    /**
     * Check a monitoring concerning its serviceState
     */
    fun check(monitoring: Monitoring): ServiceState

    /**
     * Whether a monitoring has a valid format
     */
    fun isValid(monitoring: Monitoring): Boolean

    companion object {
        /**
         * Gets the appropriate monitor for a monitoring
         */
        fun get(monitoring: Monitoring): Monitor {
            return when (monitoring.uri.scheme) {
                "http" -> HttpMonitor()
                "https" -> HttpMonitor()
                "tcp" -> TcpMonitor()
                else -> throw Exception("No monitor found for protocol '${monitoring.uri.scheme}'")
            }
        }
    }
}