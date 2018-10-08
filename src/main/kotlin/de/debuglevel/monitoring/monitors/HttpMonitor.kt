package de.debuglevel.monitoring.monitors

import de.debuglevel.monitoring.Monitoring
import de.debuglevel.monitoring.ServiceState
import mu.KotlinLogging
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URI
import java.net.UnknownHostException

class HttpMonitor : Monitor {
    override fun isValid(url: String): Boolean {
        val uri = try {
            URI(url)
        } catch (e: Exception) {
            return false
        }

        if (uri.port < 0 || uri.port > 65535) {
            return false
        }

        if (uri.scheme != "http" && uri.scheme != "https") {
            return false
        }

        return true
    }

    private val logger = KotlinLogging.logger {}

    override fun check(monitoring: Monitoring): ServiceState {
        val url = monitoring.uri.toURL()

        val state = try {
            val connection = url.openConnection() as HttpURLConnection
            SslTrustModifier.relaxHostChecking(connection)
            val statusCode = connection.responseCode

            if (statusCode < 400) ServiceState.Up else ServiceState.Down
        } catch (e: UnknownHostException) {
            ServiceState.Down
        } catch (e: ConnectException) {
            ServiceState.Down
        } catch (e: Exception) {
            logger.warn { "Unhandled exception: $e" }
            ServiceState.Down
        }

        return state
    }
}
