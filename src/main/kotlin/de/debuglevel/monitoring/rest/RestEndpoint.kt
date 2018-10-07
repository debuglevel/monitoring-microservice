package de.debuglevel.monitoring.rest

import de.debuglevel.microservices.utils.apiversion.apiVersion
import de.debuglevel.microservices.utils.spark.configuredPort
import de.debuglevel.microservices.utils.status.status
import de.debuglevel.monitoring.StateChecker
import de.debuglevel.monitoring.rest.MonitoringController.stateChecker
import mu.KotlinLogging
import spark.Spark.path
import spark.kotlin.delete
import spark.kotlin.get
import spark.kotlin.post
import kotlin.concurrent.thread

/**
 * REST endpoint
 */
class RestEndpoint {
    private val logger = KotlinLogging.logger {}

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
                MonitoringController.stateChecker.checkAll()
                val sleepTime = 60_000L
                logger.info { "Sleeping for ${sleepTime}ms" }
                Thread.sleep(sleepTime)
            }
        }

        apiVersion("1", true)
        {
            path("/monitoring") {
                get("/", "text/plain", MonitoringController.getAllPlaintext())
                get("/", "text/html", MonitoringController.getAllHtml())
                get("/", function = MonitoringController.getAllJson())
                post("/", function = MonitoringController.postOne())
                delete("/:id", function = MonitoringController.deleteOne())
            }
        }
    }
}
