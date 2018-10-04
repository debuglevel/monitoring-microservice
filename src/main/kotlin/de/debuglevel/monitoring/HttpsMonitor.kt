package de.debuglevel.monitoring

import khttp.get
import mu.KotlinLogging

class HttpsMonitor : Monitor {
    private val logger = KotlinLogging.logger {}

    override fun check(monitoring: Monitoring): Boolean {
        val response = get(monitoring.uri.toString())
        return (response.statusCode < 400)
    }
}