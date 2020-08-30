package de.debuglevel.monitoring.monitoring

import de.debuglevel.monitoring.ServiceState
import java.time.LocalDateTime

data class GetMonitoringResponse(
    val id: Int?,
    val url: String,
    val ip: String,
    val name: String,
    val createdOn: LocalDateTime,
    val lastCheck: LocalDateTime?,
    val lastModified: LocalDateTime,
    val serviceState: ServiceState,
) {
    constructor(monitoring: Monitoring) : this(
        id = monitoring.id,
        url = monitoring.url,
        ip = monitoring.ip ?: "resolving failed",
        name = monitoring.name ?: "",
        createdOn = monitoring.createdOn,
        lastCheck = monitoring.lastCheck,
        lastModified = monitoring.lastModified,
        serviceState = monitoring.serviceState
    )
}