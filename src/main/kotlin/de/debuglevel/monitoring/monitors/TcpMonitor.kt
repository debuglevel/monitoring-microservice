package de.debuglevel.monitoring.monitors

import de.debuglevel.monitoring.ServiceState
import de.debuglevel.monitoring.monitoring.Monitoring
import mu.KotlinLogging
import java.net.*

class TcpMonitor : Monitor {
    private val logger = KotlinLogging.logger {}

    override fun isValid(url: String): Boolean {
        val uri = try {
            URI(url)
        } catch (e: Exception) {
            return false
        }

        if (uri.port < 0 || uri.port > 65535) {
            return false
        }

        return true
    }

    override fun check(monitoring: Monitoring): ServiceState {
        return try {
            Socket(URI(monitoring.url).host, URI(monitoring.url).port).use {
                ServiceState.Up
            }
        } catch (e: UnknownHostException) {
            ServiceState.Down
        } catch (e: ConnectException) {
            ServiceState.Down
        } catch (e: NoRouteToHostException) {
            ServiceState.Down
        } catch (e: Exception) {
            logger.warn(e) { "Unhandled exception" }
            ServiceState.Down
        }
    }
}