package de.debuglevel.monitoring.rest

import com.google.gson.GsonBuilder
import de.debuglevel.microservices.utils.apiversion.apiVersion
import de.debuglevel.microservices.utils.spark.configuredPort
import de.debuglevel.microservices.utils.status.status
import de.debuglevel.monitoring.Monitoring
import de.debuglevel.monitoring.SummaryMonitor
import mu.KotlinLogging
import spark.Spark.path
import spark.Spark.post
import spark.kotlin.delete
import spark.kotlin.get
import kotlin.concurrent.thread


/**
 * REST endpoint
 */
class RestEndpoint {
    private val logger = KotlinLogging.logger {}

    /**
     * Starts the REST endpoint to enter a listening state
     *
     * @param args parameters to be passed from main() command line
     */

    val summaryMonitor = SummaryMonitor

    fun start(args: Array<String>) {
        logger.info("Starting...")
        configuredPort()
        status(this::class.java)

        thread {
            while (true) {
                summaryMonitor.checkAll()
                Thread.sleep(60000)
            }
        }

        apiVersion("1", true)
        {
            path("/monitoring") {
                get("/") {
                    val monitorings = summaryMonitor.monitorings

                    if (request.contentType() == "text/plain")
                    {
                        type(contentType = "text/plain")
                        monitorings.entries
                                .map { it.value }
                                .sortedBy { it.url }
                                .map { "${it.state}\t${it.lastSeenString}\t${it.url}" }.joinToString("\n")
                    }
                    else
                    {
                        type(contentType = "application/json")
                        GsonBuilder()
                                .setPrettyPrinting()
                                .create()
                                .toJson(monitorings)
                    }
                }

                post("/") { req, res ->
                    val url = if (req.contentType() == "text/plain")
                    {
                        req.body()
                    }else{
                        throw Exception("Content-Type ${req.contentType()} not supported.")
                    }

                    if (!summaryMonitor.monitorings.any { it.value.url == url })
                    {
                        val id = summaryMonitor.getNextMonitoringId()
                        val monitoring = Monitoring(id, url)
                        summaryMonitor.monitorings.put(id, monitoring)

                        res.status(201)
                        id
                    }else{
                        res.status(409)
                        "already exists"
                    }
                }

                delete("/:id") {
                    val id = request.params("id").toInt()
                    if (summaryMonitor.monitorings.remove(id) != null) {
                        response.status(201)
                        "removed"
                    }else{
                        response.status(404)
                        "not found"
                    }
                }
            }
        }
    }
}
