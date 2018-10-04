package de.debuglevel.monitoring

import mu.KotlinLogging
import java.net.Socket

class TcpMonitor : Monitor {
    private val logger = KotlinLogging.logger {}

    override fun check(monitoring: Monitoring): Boolean {
        logger.debug { "Checking ${monitoring.uri.host} on port ${monitoring.uri.port}" }
        return try {
            Socket(monitoring.uri.host, monitoring.uri.port).use {
                true
            }
        }catch(e: Exception) {
            logger.debug { e }
            false
        }
    }
}