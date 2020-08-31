package de.debuglevel.monitoring.monitoring

import de.debuglevel.monitoring.monitors.Monitor
import mu.KotlinLogging
import javax.inject.Singleton

@Singleton
class MonitoringService(
    private val monitoringRepository: MonitoringRepository
) {
    private val logger = KotlinLogging.logger {}

    fun get(id: Int): Monitoring {
        logger.debug { "Getting monitoring with ID '$id'..." }

        val monitoring: Monitoring = monitoringRepository.findById(id).orElseThrow { MonitoringNotFoundException(id) }

        logger.debug { "Got monitoring with ID '$id': $monitoring" }
        return monitoring
    }

    fun add(monitoring: Monitoring): Monitoring {
        logger.debug { "Adding monitoring '$monitoring'..." }

        if (monitoringRepository.existsByUrl(monitoring.url)) {
            throw MonitoringAlreadyExistsException(monitoring.url)
        } else if (!Monitor.get(monitoring.url).isValid(monitoring.url)) {
            throw InvalidMonitoringFormatException(monitoring.url)
        }

        val savedMonitoring = monitoringRepository.save(monitoring)

        logger.debug { "Added monitoring: $savedMonitoring" }
        return savedMonitoring
    }

    fun update(id: Int, monitoring: Monitoring): Monitoring {
        logger.debug { "Updating monitoring '$monitoring' with ID '$id'..." }

        // an object must be known to Hibernate (i.e. retrieved first) to get updated;
        // it would be a "detached entity" otherwise.
        val updateMonitoring = this.get(id).copy()
        val oldUrl = updateMonitoring.url
        updateMonitoring.apply {
            name = monitoring.name
            url = monitoring.url
            ip = monitoring.ip
            serviceState = monitoring.serviceState
            lastSeen = monitoring.lastSeen
            lastCheck = monitoring.lastCheck
        }

        if (oldUrl != monitoring.url && monitoringRepository.existsByUrl(monitoring.url)) {
            // if URL changed and the new URL does already exist
            throw MonitoringAlreadyExistsException(monitoring.url)
        } else if (!Monitor.get(monitoring.url).isValid(monitoring.url)) {
            throw InvalidMonitoringFormatException(monitoring.url)
        }

        val updatedMonitoring = monitoringRepository.update(updateMonitoring)

        logger.debug { "Updated monitoring $monitoring with ID '$id': $updatedMonitoring" }
        return updatedMonitoring
    }

    fun delete(id: Int) {
        logger.debug { "Deleting monitoring with ID '$id'..." }

        monitoringRepository.deleteById(id)

        logger.debug { "Deleted monitoring with ID '$id'" }
    }

    fun list(): Set<Monitoring> {
        logger.debug { "Getting all monitorings ..." }

        val monitorings = monitoringRepository.findAll().toSet()

        logger.debug { "Got ${monitorings.count()} monitorings" }
        return monitorings
    }

    class MonitoringNotFoundException(criteria: Any) : Exception("Monitoring '$criteria' does not exist.")
    class MonitoringAlreadyExistsException(url: String) : Exception("Monitoring with URL '$url' does already exist.")
    class InvalidMonitoringFormatException(url: String) : Exception("Monitoring with URL '$url' has an invalid format.")
}