package de.debuglevel.monitoring.monitoring

data class UpdateMonitoringRequest(
    val url: String,
    val name: String
) {
    fun toMonitoring(): Monitoring {
        return Monitoring(
            id = null,
            name = this.name,
            url = this.url
        )
    }
}