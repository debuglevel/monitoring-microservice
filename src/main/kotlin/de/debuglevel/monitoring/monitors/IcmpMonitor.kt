package de.debuglevel.monitoring.monitors

import de.debuglevel.monitoring.ServiceState
import de.debuglevel.monitoring.monitoring.Monitoring
import mu.KotlinLogging
import java.net.ConnectException
import java.net.InetAddress
import java.net.URI
import java.net.UnknownHostException

class IcmpMonitor : Monitor {
    private val logger = KotlinLogging.logger {}

    override fun isValid(url: String): Boolean {
        val uri = try {
            URI(url)
        } catch (e: Exception) {
            return false
        }

        return true
    }

    override fun check(monitoring: Monitoring): ServiceState {
        return try {
            val ip = InetAddress.getByName(URI(monitoring.url).host)
            val isReachable = ip?.isReachable(500) ?: false
            when (isReachable) {
                true -> ServiceState.Up
                else -> ServiceState.Down
            }
        } catch (e: UnknownHostException) {
            ServiceState.Down
        } catch (e: ConnectException) {
            ServiceState.Down
        } catch (e: Exception) {
            logger.warn { "Unhandled exception: $e" }
            ServiceState.Down
        }
    }
}