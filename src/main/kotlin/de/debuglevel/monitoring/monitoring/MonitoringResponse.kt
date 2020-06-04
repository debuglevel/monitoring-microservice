package de.debuglevel.monitoring.monitoring

import de.debuglevel.monitoring.Monitoring

data class MonitoringResponse(
    val id: Int,
    val url: String,
    val name: String
) {
    constructor(monitoring: Monitoring) : this(
        id = monitoring.id,
        url = monitoring.url,
        name = monitoring.name ?: ""
    )
}