package de.debuglevel.monitoring.rest

import com.google.gson.GsonBuilder
import de.debuglevel.microservices.utils.apiversion.apiVersion
import de.debuglevel.microservices.utils.spark.configuredPort
import de.debuglevel.microservices.utils.status.status
import de.debuglevel.monitoring.Monitoring
import de.debuglevel.monitoring.ServiceState
import de.debuglevel.monitoring.StateChecker
import mu.KotlinLogging
import spark.Request
import spark.Response
import spark.Spark.path
import spark.kotlin.RouteHandler
import spark.kotlin.delete
import spark.kotlin.get
import spark.kotlin.post
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

    private fun ServiceState?.toPlaintextString(): String {
        return when (this) {
            ServiceState.Up -> "[ UP ]"
            ServiceState.Down -> "[DOWN]"
            else -> "[ ?? ]"
        }
    }

    private fun ServiceState?.toHtmlString(): String {
        return when (this) {
            ServiceState.Up -> "up"
            ServiceState.Down -> "down"
            else -> "unknown"
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
                val monitorings = stateChecker.getMonitorings()

                get("/", "text/plain", processGetPlaintext(monitorings))
                get("/", "text/html", processGetHtml(monitorings))
                get("/", function = processGetJson(monitorings))
                post("/", function = processPost())
                delete("/:id", function = processDelete())
            }
        }
    }

    private fun processPost(): RouteHandler.() -> Any {
        return {
            val url = if (request.contentType() == "text/plain") {
                request.body()
            } else {
                throw Exception("Content-Type ${request.contentType()} not supported.")
            }

            try {
                val monitoring = stateChecker.addMonitoring(url)
                response.status(201)
                monitoring.id
            } catch (e: StateChecker.MonitoringAlreadyExistsException) {
                response.status(409)
                "already exists"
            }
        }
    }

    private fun processDelete(): RouteHandler.() -> String {
        return {
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

    private fun processGetJson(monitorings: Set<Monitoring>): RouteHandler.() -> String {
        return {
            logger.debug { "Processing GET request as application/json..." }
            type(contentType = "application/json")
            GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(monitorings)
        }
    }

    private fun processGetHtml(monitorings: Set<Monitoring>): RouteHandler.() -> String {
        return {
            logger.debug { "Processing GET request for text/html..." }
            type(contentType = "text/html")
            buildHtml(monitorings)
        }
    }

    private fun processGetPlaintext(monitorings: Set<Monitoring>): RouteHandler.() -> String {
        return {
            logger.debug { "Processing GET request for text/plain..." }
            type(contentType = "text/plain")
            monitorings
                    .asSequence()
                    .sortedBy { it.url }
                    .map { "${it.serviceState.toPlaintextString()}\t${it.lastSeen?.toLastSeenString()}\t${it.url}" }
                    .joinToString("\n")
        }
    }

    private fun buildHtml(monitorings: Set<Monitoring>): String {
        var html = ""

        html +=
                """
                    <!DOCTYPE html>
                    <html>
                      <head>
                        <meta charset="UTF-8">
                        <title>Monitorings</title>
                        <link href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO" crossorigin="anonymous">
                        <style>
                        tr.Up { background-color: #C4FEAC; }
                        tr.Down { background-color: #FEACAC; }
                        tr.Unknown { background-color: #EAEAEA; }
                        </style>
                      </head>
                      <body>
                      <table class="table table-hover table-sm">
                      <thead>
                      <tr>
                      <th scope="col" class="state">State</th>
                      <th scope="col" class="lastSeen">last Seen</th>
                      <th scope="col" class="url">URL</th>
                      </tr>
                      </thead>
                      <tbody>
                      """.trimIndent()

        for (monitoring in monitorings) {
            val trClass = when (monitoring.serviceState) {
                ServiceState.Up -> "table-success"
                ServiceState.Down -> "table-danger"
                else -> "table-light"
            }

            html += """
                <tr class="${monitoring.serviceState} $trClass">
                """.trimIndent()

            html +=
                    """
                        <td class="state ${monitoring.serviceState}">${monitoring.serviceState.toHtmlString()}</td>
                        <td class="lastSeen">${monitoring.lastSeen.toLastSeenString()}</td>
                        <td class="url"><a href="${monitoring.url}">${monitoring.url}</a></td>
                    """.trimIndent()

            """
                </tr>
            """.trimIndent()
        }

        html +=
                """
            </tbody>
            </table>
                      </body>
                    </html>
                """.trimIndent()

        return html
    }
}
