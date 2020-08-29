package de.debuglevel.monitoring.monitoring

import de.debuglevel.monitoring.ServiceState
import de.debuglevel.monitoring.monitors.Monitor
import io.micronaut.scheduling.annotation.Scheduled
import mu.KotlinLogging
import org.litote.kmongo.updateOne
import java.net.InetAddress
import java.time.LocalDateTime
import javax.inject.Singleton

@Singleton
class StateChecker(
    private val monitoringService: MonitoringService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Check all monitorings
     */
    @Scheduled(fixedDelay = "300s", initialDelay = "10s")
    fun checkAll() {
        logger.debug { "Checking all monitorings..." }

        monitoringService.monitorings
            .find()
            .toSet() // "escape" the Iterator of MongoCollection which would always pass the unmodified object list in a call chain
            .onEach { check(it) }
            .onEach { monitoringService.monitorings.updateOne(it) } // save modified object to MongoDB

        logger.debug { "Checked ${monitoringService.monitorings.countDocuments()} monitorings" }
    }

    /**
     * Check given monitoring
     */
    private fun check(monitoring: Monitoring) {
        logger.debug { "Checking $monitoring..." }

        val monitor = Monitor.get(monitoring)
        monitoring.serviceState = monitor.check(monitoring)
        monitoring.lastCheck = LocalDateTime.now()
        monitoring.ip = resolveHostname(monitoring)
        if (monitoring.serviceState == ServiceState.Up) {
            monitoring.lastSeen = monitoring.lastCheck
        }

        logger.debug { "Checked $monitoring: ${monitoring.serviceState}" }
    }

    private fun resolveHostname(monitoring: Monitoring): String? {
        val hostname = monitoring.uri.host
        logger.debug { "Resolving hostname '$hostname'..." }

        val address = try {
            InetAddress.getByName(hostname).hostAddress
        } catch (e: java.lang.Exception) {
            return "could not resolve"
        }

        logger.debug { "Resolved hostname '$hostname': '$address'" }

        return address
    }
}