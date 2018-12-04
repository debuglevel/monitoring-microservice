package de.debuglevel.monitoring.monitors

import de.debuglevel.monitoring.Monitoring
import de.debuglevel.monitoring.ServiceState
import mu.KotlinLogging
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URI
import java.net.UnknownHostException

class HttpMonitor : Monitor {
    private val logger = KotlinLogging.logger {}

    override fun isValid(url: String): Boolean {
        logger.debug { "Checking validity of URL '$url'..." }
        val uri = try {
            URI(url)
        } catch (e: Exception) {
            logger.debug(e) { "Checking validity of URL '$url' failed with exception." }
            return false
        }

        if (uri.port < 0 || uri.port > 65535) {
            logger.debug { "Checking validity of URL '$url' failed with port '$uri.port' too small or too large." }
            return false
        }

        if (uri.scheme != "http" && uri.scheme != "https") {
            logger.debug { "Checking validity of URL '$url' failed with scheme '$uri.scheme' != http or != https." }
            return false
        }

        logger.debug { "Checking validity of URL '$url' succeeded." }
        return true
    }

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
