package de.debuglevel.monitoring.rest.transformers

import de.debuglevel.monitoring.Monitoring
import de.debuglevel.monitoring.ServiceState
import java.time.format.DateTimeFormatter
import java.util.*

data class MonitoringViewModel(val monitoring: Monitoring) {
    val serviceStateCssTrClass
        get() = when (monitoring.serviceState) {
            ServiceState.Up -> "table-success"
            ServiceState.Down -> "table-danger"
            else -> "table-light"
        }

    val serviceStatePlainTextLabel
        get() = when (monitoring.serviceState) {
            ServiceState.Up -> "[ UP ]"
            ServiceState.Down -> "[DOWN]"
            else -> "[ ?? ]"
        }

    val serviceStateCssClass
        get() = when (monitoring.serviceState) {
            ServiceState.Up -> "up"
            ServiceState.Down -> "down"
            else -> "unknown"
        }

    val serviceStateHtmlLabel
        get() = when (monitoring.serviceState) {
            ServiceState.Up -> "up"
            ServiceState.Down -> "down"
            else -> "unknown"
        }

    val lastSeen
        get() = if (monitoring.lastSeen != null) {
            monitoring.lastSeen?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH))
        } else {
            "never"
        }

    val url get() = monitoring.url
}