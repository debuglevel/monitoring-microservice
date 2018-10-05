package de.debuglevel.monitoring.rest

import com.google.gson.GsonBuilder
import de.debuglevel.microservices.utils.apiversion.apiVersion
import de.debuglevel.microservices.utils.spark.configuredPort
import de.debuglevel.microservices.utils.status.status
import de.debuglevel.monitoring.ServiceState
import de.debuglevel.monitoring.StateChecker
import mu.KotlinLogging
import spark.Spark.path
import spark.Spark.post
import spark.kotlin.delete
import spark.kotlin.get
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.thread

/**
 * REST endpoint
 */
class RestEndpoint {
    private val logger = KotlinLogging.logger {}

    val stateChecker = StateChecker

    private fun ServiceState?.stateToString(): String {
        return when (this) {
            ServiceState.Up -> "[ UP ]"
            ServiceState.Down -> "[DOWN]"
            else -> "[ ?? ]"
        }
    }

    private fun LocalDateTime?.toLastSeenString(): String {
        return if (this != null) {
            this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH))
        } else {
            "never"
        }
    }

    /**
     * Starts the REST endpoint to enter a listening serviceState
     *
     * @param args parameters to be passed from main() command line
     */
    fun start(args: Array<String>) {
        logger.info("Starting...")
        configuredPort()
        status(this::class.java)

        thread {
            while (true) {
                stateChecker.checkAll()
                val sleepTime = 60_000L
                logger.info { "Sleeping for ${sleepTime}ms" }
                Thread.sleep(sleepTime)
            }
        }

        apiVersion("1", true)
        {
            path("/monitoring") {
                get("/") {
                    val monitorings = stateChecker.getMonitorings()

                    if (request.contentType() == "text/plain") {
                        type(contentType = "text/plain")
                        monitorings
                                .asSequence()
                                .sortedBy { it.url }
                                .map { "${it.serviceState.stateToString()}\t${it.lastSeen?.toLastSeenString()}\t${it.url}" }
                                .joinToString("\n")
                    } else {
                        type(contentType = "application/json")
                        GsonBuilder()
                                .setPrettyPrinting()
                                .create()
                                .toJson(monitorings)
                    }
                }

                post("/") { req, res ->
                    val url = if (req.contentType() == "text/plain") {
                        req.body()
                    } else {
                        throw Exception("Content-Type ${req.contentType()} not supported.")
                    }

                    try {
                        val monitoring = stateChecker.addMonitoring(url)
                        res.status(201)
                        monitoring.id
                    } catch (e: StateChecker.MonitoringAlreadyExistsException) {
                        res.status(409)
                        "already exists"
                    }
                }

                delete("/:id") {
                    val id = request.params("id").toInt()

                    try {
                        stateChecker.removeMonitoring(id)
                        response.status(201)
                        "removed"
                    } catch (e: StateChecker.MonitoringNotFoundException) {
                        response.status(404)
                        "not found"
                    }
                }
            }
        }
    }
}
