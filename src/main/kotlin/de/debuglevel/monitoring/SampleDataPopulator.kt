package de.debuglevel.monitoring

import de.debuglevel.monitoring.monitoring.Monitoring
import de.debuglevel.monitoring.monitoring.MonitoringService
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import mu.KotlinLogging
import javax.inject.Singleton

@Singleton
class SampleDataPopulator(private val monitoringService: MonitoringService) :
    ApplicationEventListener<ServerStartupEvent> {
    private val logger = KotlinLogging.logger {}

    override fun onApplicationEvent(event: ServerStartupEvent) {
        logger.debug { "Adding sample monitorings if no monitorings exist..." }

        val monitoringsCount = monitoringService.list().count()
        if (monitoringsCount > 0) {
            logger.debug { "Not adding sample monitorings, as there are $monitoringsCount monitorings present" }
            return
        }

        setOf(
            Monitoring(id = null, name = "Google HTTP", url = "http://www.google.de"),
            Monitoring(id = null, name = "Google HTTPS", url = "https://www.google.de"),
            Monitoring(id = null, name = "Google TCP 80", url = "tcp://www.google.de:80"),
            Monitoring(id = null, name = "Google TCP 12345", url = "tcp://www.google.de:12345"),
        ).forEach { monitoringService.add(it) }
    }
}