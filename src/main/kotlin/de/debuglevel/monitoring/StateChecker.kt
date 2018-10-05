package de.debuglevel.monitoring

import com.mongodb.client.MongoCollection
import de.debuglevel.monitoring.monitors.Monitor
import mu.KotlinLogging
import org.litote.kmongo.KMongo
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import org.litote.kmongo.updateOne
import java.time.LocalDateTime

object StateChecker {
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
        val mongoClient = KMongo.createClient(Configuration.mongodbUrl)
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
    fun addMonitoring(url: String): Monitoring {
        logger.debug { "Adding $url..." }
        if (!monitorings.find(Monitoring::url eq url).any()) {
            val monitoring = Monitoring(this.nextMonitoringId, url)
            monitorings.insertOne(monitoring)
            logger.debug { "Adding $url done" }
            return monitoring
        } else {
            logger.debug { "Monitoring $url not added, as it does already exist." }
            throw MonitoringAlreadyExistsException(url)
        }
    }

    class MonitoringAlreadyExistsException(url: String) : Exception("Monitoring with URL '$url' already exists.")

    /**
     * Removes a monitoring if it exists.
     */
    fun removeMonitoring(id: Int) {
        logger.debug { "Removing $id..." }
        if (!monitorings.find(Monitoring::id eq id).any()) {
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
    fun checkAll() {
        logger.debug { "Checking all monitorings..." }

        monitorings.find()
                .toSet() // "escape" the Iterator of MongoCollection which would always pass the unmodified object list in a call chain
                .onEach { check(it) }
                .onEach { monitorings.updateOne(it) } // save modified object to MongoDB

        logger.debug { "Checking all monitorings done" }
    }

    /**
     * Check given monitoring
     */
    private fun check(monitoring: Monitoring) {
        logger.debug { "Checking $monitoring..." }

        val monitor = Monitor.get(monitoring)
        monitoring.serviceState = monitor.check(monitoring)
        monitoring.lastCheck = LocalDateTime.now()
        if (monitoring.serviceState == ServiceState.Up) {
            monitoring.lastSeen = monitoring.lastCheck
        }

        logger.debug { "Checked $monitoring: ${monitoring.serviceState}" }
    }
}