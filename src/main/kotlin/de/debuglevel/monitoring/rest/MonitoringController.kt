package de.debuglevel.monitoring.rest

import de.debuglevel.monitoring.Monitoring
import de.debuglevel.monitoring.StateChecker
import de.debuglevel.monitoring.rest.transformers.JsonTransformer
import de.debuglevel.monitoring.rest.transformers.MonitoringsTransformer
import mu.KotlinLogging
import spark.kotlin.RouteHandler

object MonitoringController {
    private val logger = KotlinLogging.logger {}

    val stateChecker = StateChecker

    fun postOne(): RouteHandler.() -> Any {
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

    fun deleteOne(): RouteHandler.() -> String {
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

    fun getAllJson(): RouteHandler.() -> String {
        return {
            logger.debug { "Processing GET request as application/json..." }
            type(contentType = "application/json")
            JsonTransformer.render(stateChecker.getMonitorings())
        }
    }

    fun getAllHtml(): RouteHandler.() -> String {
        return {
            logger.debug { "Processing GET request for text/html..." }
            type(contentType = "text/html")
            MonitoringsTransformer.toHtml(stateChecker.getMonitorings())
        }
    }

    fun getAllPlaintext(): RouteHandler.() -> String {
        return {
            logger.debug { "Processing GET request for text/plain..." }
            type(contentType = "text/plain")
            MonitoringsTransformer.toPlaintext(stateChecker.getMonitorings())
        }
    }
}