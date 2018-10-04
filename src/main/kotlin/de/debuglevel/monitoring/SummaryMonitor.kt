package de.debuglevel.monitoring

import mu.KotlinLogging
import org.mapdb.DBMaker
import org.mapdb.Serializer
import java.time.LocalDateTime

object SummaryMonitor {
    private val logger = KotlinLogging.logger {}

    val monitorings = mutableMapOf<Int,Monitoring>()

    fun getNextMonitoringId() = monitorings.keys.max()?.plus(1) ?: 1

    fun checkAll() {
        logger.debug { "Checking all monitorings..." }
        monitorings.forEach { check(it.value) }
        logger.debug { "Checking all monitorings done" }
    }

    private fun check(monitoring: Monitoring)
    {
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
        return when (monitoring.uri.scheme)
        {
            "http" -> HttpMonitor()
            "https" -> HttpMonitor()
            "tcp" -> TcpMonitor()
            else -> throw Exception("No monitor found for protocol '${monitoring.uri.scheme}'")
        }
    }
}