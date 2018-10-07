package de.debuglevel.monitoring.rest.transformers

import de.debuglevel.monitoring.Monitoring
import de.debuglevel.monitoring.ServiceState
import mu.KotlinLogging
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object MonitoringsTransformer {
    private val logger = KotlinLogging.logger {}

    fun toPlaintext(monitorings: Set<Monitoring>): String {
        return monitorings
                .asSequence()
                .sortedBy { it.url }
                .map { "${it.serviceState.toPlaintextString()}\t${it.lastSeen?.toLastSeenString()}\t${it.url}" }
                .joinToString("\n")
    }

    fun toHtml(monitorings: Set<Monitoring>): String {
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
}