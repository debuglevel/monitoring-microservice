package de.debuglevel.monitoring.monitoring

import de.debuglevel.monitoring.Monitoring
import de.debuglevel.monitoring.StateChecker
import de.debuglevel.monitoring.monitors.Monitor
import de.debuglevel.monitoring.rest.transformers.MonitoringViewModel
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.views.View
import mu.KotlinLogging

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/monitorings")
class MonitoringController(private val stateChecker: StateChecker) {
    private val logger = KotlinLogging.logger {}

    @Post("/")
    fun postOne(monitoringRequest: MonitoringRequest): HttpResponse<*> {
        logger.debug("Called postOne($monitoringRequest)")

        return try {
            val monitoring = stateChecker.addMonitoring(monitoringRequest)
            HttpResponse.created(monitoring)
        } catch (e: StateChecker.MonitoringAlreadyExistsException) {
            HttpResponse.badRequest("already exists")
        } catch (e: StateChecker.InvalidMonitoringFormatException) {
            HttpResponse.badRequest("supplied URL is invalid: ${e.message}")
        } catch (e: Monitor.EmptyMonitoringProtocolException) {
            HttpResponse.badRequest("protocol must be supplied")
        } catch (e: Monitor.UnsupportedMonitoringProtocolException) {
            HttpResponse.badRequest("protocol is not supported")
        }
    }

    @Put("/{id}")
    fun putOne(id: Int, monitoringRequest: MonitoringRequest): HttpResponse<*> {
        logger.debug("Called putOne($id, $monitoringRequest)")

        return try {
            val monitoring = stateChecker.updateMonitoring(id, monitoringRequest)
            HttpResponse.ok(monitoring)
        } catch (e: StateChecker.MonitoringNotFoundException) {
            HttpResponse.notFound("not found")
        } catch (e: StateChecker.InvalidMonitoringFormatException) {
            HttpResponse.badRequest("supplied URL is invalid: ${e.message}")
        } catch (e: Monitor.EmptyMonitoringProtocolException) {
            HttpResponse.badRequest("protocol must be supplied")
        } catch (e: Monitor.UnsupportedMonitoringProtocolException) {
            HttpResponse.badRequest("protocol is not supported")
        }
    }

    @Delete("/{id}")
    fun deleteOne(id: Int): HttpResponse<*> {
        logger.debug("Called deleteOne($id)")

        return try {
            stateChecker.removeMonitoring(id)
            HttpResponse.ok("removed")
        } catch (e: StateChecker.MonitoringNotFoundException) {
            HttpResponse.ok("not found")
        }
    }

    @Get("/", produces = [MediaType.APPLICATION_JSON])
    fun getAll(): HttpResponse<Set<Monitoring>> {
        logger.debug("Called getAll()")

        return try {
            val monitorings = stateChecker.getMonitorings()
            HttpResponse.ok(monitorings)
        } catch (e: Exception) {
            logger.error(e) { "Unhandled exception" }
            HttpResponse.serverError<Set<Monitoring>>()
        }
    }

    @View("list.html")
    @Get("/html", produces = [MediaType.TEXT_HTML])
    fun getAllHtml(): HttpResponse<*> {
        logger.debug("Called getAllHtml()")

        return try {
            val monitorings = stateChecker.getMonitorings()
                .sortedWith(compareBy({ it.name }, { it.uri.host }, { it.url }))
                .map { MonitoringViewModel(it) }

            HttpResponse.ok(mapOf("monitorings" to monitorings))
        } catch (e: Exception) {
            logger.error(e) { "Unhandled exception" }
            HttpResponse.serverError<String>(e.toString())
        }
    }

    // TODO: does not seem to use view but just emit the map
    @View("list.plaintext")
    @Get("/plaintext", produces = [MediaType.TEXT_PLAIN])
    fun getAllPlaintext(): HttpResponse<*> {
        logger.debug("Called getAllPlaintext()")

        return try {
            val monitorings = stateChecker.getMonitorings()
                .sortedWith(compareBy({ it.name }, { it.uri.host }, { it.url }))
                .map { MonitoringViewModel(it) }

            HttpResponse.ok(mapOf("monitorings" to monitorings))
        } catch (e: Exception) {
            logger.error(e) { "Unhandled exception" }
            HttpResponse.serverError<String>(e.toString())
        }
    }
}