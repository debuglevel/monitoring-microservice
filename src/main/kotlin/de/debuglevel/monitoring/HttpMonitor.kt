package de.debuglevel.monitoring

import mu.KotlinLogging
import java.net.HttpURLConnection
import java.net.UnknownHostException


class HttpMonitor : Monitor {
    private val logger = KotlinLogging.logger {}

    override fun check(monitoring: Monitoring): Boolean {
        val url = monitoring.uri.toURL()

        val success = try {
            val connection = url.openConnection() as HttpURLConnection
            TrustModifier.relaxHostChecking(connection)
            val statusCode = connection.responseCode

            (statusCode < 400)
        } catch (e: UnknownHostException) {
            false
        } catch (e: Exception) {
            logger.warn { "Unhandled exception: $e" }
            false
        }

        return success
    }

}
