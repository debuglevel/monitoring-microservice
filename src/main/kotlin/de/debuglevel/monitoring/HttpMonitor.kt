package de.debuglevel.monitoring

import khttp.get
import mu.KotlinLogging
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL


class HttpMonitor : Monitor {
    private val logger = KotlinLogging.logger {}

    override fun check(monitoring: Monitoring): Boolean {
        val url = monitoring.uri.toURL()
        val connection = url.openConnection() as HttpURLConnection
        TrustModifier.relaxHostChecking(connection); // here's where the magic happens

        //connection.setDoOutput(true);
        val statusCode = connection.responseCode


        return (statusCode < 400)
    }

}
