package de.debuglevel.monitoring.monitoring

import de.debuglevel.monitoring.ServiceState
import de.debuglevel.monitoring.monitors.Monitor
import io.micronaut.scheduling.annotation.Scheduled
import mu.KotlinLogging
import java.net.InetAddress
import java.net.URI
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
    // TODO: define delay via configuration
    @Scheduled(fixedDelay = "300s", initialDelay = "10s")
    fun checkAll() {
        logger.debug { "Checking all monitorings..." }

        val monitorings = monitoringService.list()

        monitorings
            .onEach { check(it) }
            .onEach { monitoringService.update(it.id!!, it) }

        logger.debug { "Checked ${monitorings.count()} monitorings" }
    }

    /**
     * Check given monitoring
     */
    private fun check(monitoring: Monitoring) {
        logger.debug { "Checking $monitoring..." }

        val monitor = Monitor.get(monitoring.url)
        monitoring.serviceState = monitor.check(monitoring)
        monitoring.lastCheck = LocalDateTime.now()
        monitoring.ip = resolveHostname(monitoring)
        if (monitoring.serviceState == ServiceState.Up) {
            monitoring.lastSeen = monitoring.lastCheck
        }

        logger.debug { "Checked $monitoring: ${monitoring.serviceState}" }
    }

    private fun resolveHostname(monitoring: Monitoring): String? {
        val hostname = URI(monitoring.url).host
        logger.debug { "Resolving hostname '$hostname'..." }

        val ip = try {
            InetAddress.getByName(hostname).hostAddress
        } catch (e: java.lang.Exception) {
            logger.warn(e) { "Could not resolve hostname $hostname to IP" }
            return "could not resolve hostname"
        }

        logger.debug { "Resolved hostname '$hostname': '$ip'" }
        return ip
    }
}