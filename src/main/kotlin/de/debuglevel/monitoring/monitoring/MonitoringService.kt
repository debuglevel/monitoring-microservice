package de.debuglevel.monitoring.monitoring

import com.mongodb.client.MongoCollection
import de.debuglevel.monitoring.monitors.Monitor
import io.micronaut.context.annotation.Property
import mu.KotlinLogging
import org.litote.kmongo.*
import javax.inject.Singleton

@Singleton
class MonitoringService(
    @Property(name = "mongodb.uri") private val mongodbUri: String
) {
    private val logger = KotlinLogging.logger {}

    val monitorings: MongoCollection<Monitoring>

    /**
     * Returns the next free monitoring ID or 1 if there is no monitoring so far
     */
    private val nextMonitoringId
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
    fun addMonitoring(addMonitoringRequest: AddMonitoringRequest): Monitoring {
        logger.debug { "Adding $addMonitoringRequest..." }
        if (!monitorings.find(Monitoring::url eq addMonitoringRequest.url).any()) {
            if (Monitor.get(addMonitoringRequest.url).isValid(addMonitoringRequest.url)) {
                val monitoring =
                    Monitoring(this.nextMonitoringId, addMonitoringRequest.url, addMonitoringRequest.name)
                monitorings.insertOne(monitoring)
                logger.debug { "Adding $addMonitoringRequest done" }
                return monitoring
            } else {
                logger.debug { "Adding $addMonitoringRequest failed as URL is invalid." }
                throw InvalidMonitoringFormatException(addMonitoringRequest.url)
            }
        } else {
            logger.debug { "Monitoring $addMonitoringRequest not added, as it does already exist." }
            throw MonitoringAlreadyExistsException(addMonitoringRequest.url)
        }
    }

    /**
     * Updates a monitoring if it does already exist.
     */
    fun updateMonitoring(id: Int, monitoringRequest: UpdateMonitoringRequest): Monitoring {
        logger.debug { "Updating $id with $monitoringRequest..." }
        val monitoring = monitorings.findOne(Monitoring::id eq id)
        if (monitoring != null) {
            if (Monitor.get(monitoringRequest.url).isValid(monitoringRequest.url)) {
                monitoring.name = monitoringRequest.name
                monitoring.url = monitoringRequest.url
                monitorings.updateOne(monitoring)
                logger.debug { "Updating $id done" }
                return monitoring
            } else {
                logger.debug { "Updating $id failed as URL is invalid." }
                throw InvalidMonitoringFormatException(monitoringRequest.url)
            }
        } else {
            logger.debug { "Monitoring $monitoringRequest not updated, as it does not exist." }
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
}