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
            val socket = Socket()
            val timeout = 500
            socket.soTimeout = timeout
            try {
                logger.debug { "Connecting to socket with timeout ${timeout}ms..." }
                socket.connect(InetSocketAddress(URI(monitoring.url).host, URI(monitoring.url).port), timeout)
                logger.debug { "Connected to socket" }
                ServiceState.Up
            } catch (e: Exception) {
                logger.debug { "Connection to socket failed: ${e.message}" }
                logger.debug { "Host is down due to: ${e.message}" }
                ServiceState.Down
            } finally {
                logger.debug { "Closing socket..." }
                socket.close()
                logger.debug { "Closed socket" }
            }
        } catch (e: UnknownHostException) {
            logger.debug { "Host is down due to: ${e.message}" }
            ServiceState.Down
        } catch (e: ConnectException) {
            logger.debug { "Host is down due to: ${e.message}" }
            ServiceState.Down
        } catch (e: NoRouteToHostException) {
            logger.debug { "Host is down due to: ${e.message}" }
            ServiceState.Down
        } catch (e: Exception) {
            logger.warn(e) { "Unhandled exception" }
            ServiceState.Down
        }
    }
}