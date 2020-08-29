package de.debuglevel.monitoring.monitoring

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

        // TODO: prevent adding a monitoring with an existing URL
        // TODO: prevent adding a monitoring with an invalid URL
        val savedMonitoring = monitoringRepository.save(monitoring)

        logger.debug { "Added monitoring: $savedMonitoring" }
        return savedMonitoring
    }

    fun update(id: Int, monitoring: Monitoring): Monitoring {
        logger.debug { "Updating monitoring '$monitoring' with ID '$id'..." }

        // an object must be known to Hibernate (i.e. retrieved first) to get updated;
        // it would be a "detached entity" otherwise.
        val updateMonitoring = this.get(id).apply {
            name = monitoring.name
            url = monitoring.url
        }

        val updatedMonitoring = monitoringRepository.update(updateMonitoring)

        logger.debug { "Updated monitoring: $updatedMonitoring with ID '$id'" }
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