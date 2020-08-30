package de.debuglevel.monitoring.monitoring

import de.debuglevel.monitoring.monitors.Monitor
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.views.View
import mu.KotlinLogging
import java.net.URI

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/monitorings")
class MonitoringController(
    private val monitoringService: MonitoringService,
    private val stateChecker: StateChecker,
) {
    private val logger = KotlinLogging.logger {}

    @Post("/")
    fun postOne(addMonitoringRequest: AddMonitoringRequest): HttpResponse<*> {
        logger.debug("Called postOne($addMonitoringRequest)")

        return try {
            val monitoring = monitoringService.add(addMonitoringRequest.toMonitoring())
            HttpResponse.created(monitoring)
        } catch (e: MonitoringService.MonitoringAlreadyExistsException) {
            HttpResponse.badRequest("Monitoring does already exist")
        } catch (e: MonitoringService.InvalidMonitoringFormatException) {
            HttpResponse.badRequest("Supplied URL is invalid: ${e.message}")
        } catch (e: Monitor.UnsupportedMonitoringProtocolException) {
            HttpResponse.badRequest("Protocol '${e.scheme}' is not supported")
        }
    }

    @Put("/{id}")
    fun putOne(id: Int, updateMonitoringRequest: UpdateMonitoringRequest): HttpResponse<*> {
        logger.debug("Called putOne($id, $updateMonitoringRequest)")

        return try {
            val monitoring = monitoringService.update(id, updateMonitoringRequest.toMonitoring())
            HttpResponse.ok(monitoring)
        } catch (e: MonitoringService.MonitoringNotFoundException) {
            HttpResponse.notFound("Monitoring does not exist")
        } catch (e: MonitoringService.InvalidMonitoringFormatException) {
            HttpResponse.badRequest("Supplied URL is invalid: ${e.message}")
        } catch (e: Monitor.UnsupportedMonitoringProtocolException) {
            HttpResponse.badRequest("Protocol '${e.scheme}' is not supported")
        }
    }

    @Delete("/{id}")
    fun deleteOne(id: Int): HttpResponse<*> {
        logger.debug("Called deleteOne($id)")

        return try {
            monitoringService.delete(id)
            HttpResponse.ok("Deleted monitoring $id")
        } catch (e: MonitoringService.MonitoringNotFoundException) {
            HttpResponse.ok("Monitoring $id does not exist")
        }
    }

    @Get("/", produces = [MediaType.APPLICATION_JSON])
    fun getAll(): HttpResponse<*> {
        logger.debug("Called getAll()")

        return try {
            val monitoringResponses = monitoringService.list()
                .map { GetMonitoringResponse(it) }
            HttpResponse.ok(monitoringResponses)
        } catch (e: Exception) {
            logger.error(e) { "Unhandled exception" }
            HttpResponse.serverError("Unhandled exception: ${e.stackTrace}")
        }
    }

    @View("list.html")
    @Get("/html", produces = [MediaType.TEXT_HTML])
    fun getAllHtml(): HttpResponse<*> {
        logger.debug("Called getAllHtml()")

        return try {
            val monitorings = monitoringService.list()
                .sortedWith(compareBy({ it.name }, { URI(it.url).host }, { it.url }))
                .map { MonitoringViewModel(it) }

            HttpResponse.ok(mapOf("monitorings" to monitorings))
        } catch (e: Exception) {
            logger.error(e) { "Unhandled exception" }
            HttpResponse.serverError("Unhandled exception: ${e.stackTrace}")
        }
    }

    @View("list.plaintext")
    @Get("/plaintext", produces = [MediaType.TEXT_HTML])
    //@Get("/plaintext", produces = [MediaType.TEXT_PLAIN]) // TODO: see https://github.com/micronaut-projects/micronaut-views/issues/56
    fun getAllPlaintext(): HttpResponse<*> {
        logger.debug("Called getAllPlaintext()")

        return try {
            val monitorings = monitoringService.list()
                .sortedWith(compareBy({ it.name }, { URI(it.url).host }, { it.url }))
                .map { MonitoringViewModel(it) }

            HttpResponse.ok(mapOf("monitorings" to monitorings))
        } catch (e: Exception) {
            logger.error(e) { "Unhandled exception" }
            HttpResponse.serverError("Unhandled exception: ${e.stackTrace}")
        }
    }
}