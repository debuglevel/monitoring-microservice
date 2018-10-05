package de.debuglevel.monitoring

import com.mongodb.client.MongoCollection
import mu.KotlinLogging
import org.litote.kmongo.KMongo
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import org.litote.kmongo.updateOne
import java.time.LocalDateTime

object SummaryMonitor {
    private val logger = KotlinLogging.logger {}

    val monitorings: MongoCollection<Monitoring>

    fun getNextMonitoringId() = monitorings.find().map { it.id }.max()?.plus(1) ?: 1

    init {
        val mongoClient = KMongo.createClient(Configuration.mongodbUrl)
        val database = mongoClient.getDatabase("monitoring")
        monitorings = database.getCollection<Monitoring>()
    }

    fun addMonitoring(monitoring: Monitoring) {
        monitorings.insertOne(monitoring)
    }

    fun removeMonitoring(id: Int): Boolean {
        val result = monitorings.deleteOne(Monitoring::id eq id)
        return result.deletedCount >= 1
    }

    fun checkAll() {
        logger.debug { "Checking all monitorings..." }

        monitorings.find()
                .toSet() // "escape" the Iterator of MongoCollection which would always pass the unmodified object list in a call chain
                .onEach { check(it) }
                .onEach { monitorings.updateOne(it) }

        logger.debug { "Checking all monitorings done" }
    }

    private fun check(monitoring: Monitoring) {
        logger.debug { "Checking $monitoring..." }

        val monitor = getMonitor(monitoring)
        val result = monitor.check(monitoring)
        monitoring.lastCheckOkay = result
        if (result) {
            monitoring.lastSeen = LocalDateTime.now()
        }

        logger.debug { "Checking $monitoring: $result" }
    }

    private fun getMonitor(monitoring: Monitoring): Monitor {
        return when (monitoring.uri.scheme) {
            "http" -> HttpMonitor()
            "https" -> HttpMonitor()
            "tcp" -> TcpMonitor()
            else -> throw Exception("No monitor found for protocol '${monitoring.uri.scheme}'")
        }
    }
}