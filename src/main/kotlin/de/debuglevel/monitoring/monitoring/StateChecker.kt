package de.debuglevel.monitoring.monitoring

import de.debuglevel.monitoring.ServiceState
import de.debuglevel.monitoring.monitors.Monitor
import io.micronaut.scheduling.annotation.Scheduled
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.net.InetAddress
import java.net.URI
import java.time.LocalDateTime
import javax.inject.Singleton
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@Singleton
class StateChecker(
    private val monitoringService: MonitoringService,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Check all monitorings
     */
    @ExperimentalTime
    @Scheduled(
        fixedDelay = "\${app.monitoring.checks.delay:60s}",
        initialDelay = "\${app.monitoring.checks.initial-delay:10s}"
    )
    fun checkAll() {
        logger.debug { "Checking all monitorings..." }

        val monitorings = monitoringService.list()
        val duration = measureTime {
            runBlocking {
                monitorings
                    .map {
                        // Use coroutines to run checks non-blocking/concurrently.
                        GlobalScope.launch {
                            check(it)
                            monitoringService.update(it.id!!, it)
                        }//.join() // DEBUG: Use a join() here to prevent concurrent execution
                    }
                    .map { it.join() }
            }
        }

        logger.debug { "Checked ${monitorings.count()} monitorings in $duration" }
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