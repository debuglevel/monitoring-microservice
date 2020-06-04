package de.debuglevel.monitoring

import com.mongodb.client.MongoCollection
import de.debuglevel.monitoring.monitoring.MonitoringAddUpdateRequest
import de.debuglevel.monitoring.monitors.Monitor
import io.micronaut.context.annotation.Property
import io.micronaut.scheduling.annotation.Scheduled
import mu.KotlinLogging
import org.litote.kmongo.*
import java.net.InetAddress
import java.time.LocalDateTime
import javax.inject.Singleton

@Singleton
class StateChecker(
    @Property(name = "mongodb.uri") val mongodbUri: String
) {
    private val logger = KotlinLogging.logger {}

    private val monitorings: MongoCollection<Monitoring>

    /**
     * Returns the next free monitoring ID or 1 if there is no monitoring so far
     */
    val nextMonitoringId
        get() = monitorings.find()
                .map { it.id }
                .max()
                ?.plus(1) ?: 1

    init {
        val mongoClient = KMongo.createClient(mongodbUri)
        val database = mongoClient.getDatabase("monitoring")
        monitorings = database.getCollection<Monitoring>()
    }

    /**
     * Gets all monitorings
     */
    fun getMonitorings() = monitorings.find().toSet()

    /**
     * Adds a monitoring if it does not already exist.
     */
    fun addMonitoring(monitoringAddUpdateRequest: MonitoringAddUpdateRequest): Monitoring {
        logger.debug { "Adding $monitoringAddUpdateRequest..." }
        if (!monitorings.find(Monitoring::url eq monitoringAddUpdateRequest.url).any()) {
            if (Monitor.get(monitoringAddUpdateRequest.url).isValid(monitoringAddUpdateRequest.url))
            {
                val monitoring =
                    Monitoring(this.nextMonitoringId, monitoringAddUpdateRequest.url, monitoringAddUpdateRequest.name)
                monitorings.insertOne(monitoring)
                logger.debug { "Adding $monitoringAddUpdateRequest done" }
                return monitoring
            }else{
                logger.debug { "Adding $monitoringAddUpdateRequest failed as URL is invalid." }
                throw InvalidMonitoringFormatException(monitoringAddUpdateRequest.url)
            }
        } else {
            logger.debug { "Monitoring $monitoringAddUpdateRequest not added, as it does already exist." }
            throw MonitoringAlreadyExistsException(monitoringAddUpdateRequest.url)
        }
    }

    /**
     * Updates a monitoring if it does already exist.
     */
    fun updateMonitoring(id: Int, monitoringAddUpdateRequest: MonitoringAddUpdateRequest): Monitoring {
        logger.debug { "Updating $id with $monitoringAddUpdateRequest..." }
        val monitoring = monitorings.findOne(Monitoring::id eq id)
        if (monitoring != null) {
            if (Monitor.get(monitoringAddUpdateRequest.url).isValid(monitoringAddUpdateRequest.url)) {
                monitoring.name = monitoringAddUpdateRequest.name
                monitoring.url = monitoringAddUpdateRequest.url
                monitorings.updateOne(monitoring)
                logger.debug { "Updating $id done" }
                return monitoring
            } else {
                logger.debug { "Updating $id failed as URL is invalid." }
                throw InvalidMonitoringFormatException(monitoringAddUpdateRequest.url)
            }
        } else {
            logger.debug { "Monitoring $monitoringAddUpdateRequest not updated, as it does not exist." }
            throw MonitoringNotFoundException(id)
        }
    }

    class MonitoringAlreadyExistsException(url: String) : Exception("Monitoring with URL '$url' already exists.")
    class InvalidMonitoringFormatException(url: String) : Exception("Monitoring with URL '$url' has an invalid format.")

    /**
     * Removes a monitoring if it exists.
     */
    fun removeMonitoring(id: Int) {
        logger.debug { "Removing $id..." }
        if (monitorings.find(Monitoring::id eq id).any()) {
            val result = monitorings.deleteMany(Monitoring::id eq id)
            if (result.deletedCount <= 0) {
                logger.debug { "Removing monitoring with $id did not succeed." }
                throw Exception("${result.deletedCount} monitorings deleted although one with id $id was found.")
            }
        } else {
            logger.debug { "Monitoring with $id not removed, as it was not found." }
            throw MonitoringNotFoundException(id)
        }
        logger.debug { "Removing $id done" }
    }

    class MonitoringNotFoundException(id: Int) : Exception("No monitoring with id $id found.")

    /**
     * Check all monitorings
     */
    @Scheduled(fixedDelay = "300s", initialDelay = "10s")
    fun checkAll() {
        logger.debug { "Checking all monitorings..." }

        monitorings.find()
                .toSet() // "escape" the Iterator of MongoCollection which would always pass the unmodified object list in a call chain
                .onEach { check(it) }
                .onEach { monitorings.updateOne(it) } // save modified object to MongoDB

        logger.debug { "Checked all monitorings" }
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