package de.debuglevel.monitoring.monitors

import de.debuglevel.monitoring.Monitoring
import de.debuglevel.monitoring.ServiceState
import mu.KotlinLogging
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

class HttpMonitor : Monitor {
    private val logger = KotlinLogging.logger {}

    override fun isValid(urlString: String): Boolean {
        logger.debug { "Checking validity of URL '$urlString'..." }

        // using Apache Commons Validator would be nice, but it cannot disable failing on custom TLDs.
        val url = try {
            val urlObj = URL(urlString)
            logger.debug { "Converted to URI with protocol '${urlObj.protocol}', host '${urlObj.host}', port '${urlObj.port}', path '${urlObj.path}', query '${urlObj.query}'..." }
            urlObj
        } catch (e: Exception) {
            logger.debug(e) { "Checking validity of URL '$urlString' failed with exception." }
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
