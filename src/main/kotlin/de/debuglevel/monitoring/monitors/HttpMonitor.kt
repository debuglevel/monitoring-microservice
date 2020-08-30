package de.debuglevel.monitoring.monitors

import de.debuglevel.monitoring.ServiceState
import de.debuglevel.monitoring.monitoring.Monitoring
import mu.KotlinLogging
import java.net.*

class HttpMonitor : Monitor {
    private val logger = KotlinLogging.logger {}

    override fun isValid(url: String): Boolean {
        logger.debug { "Checking validity of URL '$url'..." }

        // using Apache Commons Validator would be nice, but it cannot disable failing on custom TLDs.
        val url = try {
            val url = URL(url)
            logger.debug { "Converted to URI with protocol '${url.protocol}', host '${url.host}', port '${url.port}', path '${url.path}', query '${url.query}'..." }
            url
        } catch (e: Exception) {
            logger.debug(e) { "Checking validity of URL '$url' failed with exception." }
            return false
        }

        if (url.host.isNullOrBlank()) {
            logger.debug { "Checking validity of URL '$url' failed with host '${url.host}' empty." }
            return false
        }

        // if port is not set, it is -1. It's the responsibility of htp HTTP library to use the default port.
        if ((url.port != -1) && (url.port < 0 || url.port > 65535)) {
            logger.debug { "Checking validity of URL '$url' failed with port '${url.port}' too small or too large." }
            return false
        }

        if (url.protocol != "http" && url.protocol != "https") {
            logger.debug { "Checking validity of URL '$url' failed with scheme '${url.protocol}' != http or != https." }
            return false
        }

        logger.debug { "Checking validity of URL '$url' succeeded." }
        return true
    }

    override fun check(monitoring: Monitoring): ServiceState {
        logger.debug { "Checking $monitoring..." }

        val url = URI(monitoring.url).toURL()

        val state = try {
            logger.debug { "Opening connection on '$url'..." }
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

        logger.debug { "Checked $monitoring: $state" }
        return state
    }
}
