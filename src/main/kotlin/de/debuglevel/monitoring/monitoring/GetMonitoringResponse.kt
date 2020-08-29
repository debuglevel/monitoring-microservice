package de.debuglevel.monitoring.monitoring

data class GetMonitoringResponse(
    val id: Int?,
    val url: String,
    val name: String
) {
    constructor(monitoring: Monitoring) : this(
        id = monitoring.id,
        url = monitoring.url,
        name = monitoring.name ?: ""
    )
}