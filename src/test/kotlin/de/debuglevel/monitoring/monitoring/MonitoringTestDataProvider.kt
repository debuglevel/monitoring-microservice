package de.debuglevel.monitoring.monitoring

import de.debuglevel.monitoring.monitors.Monitor
import mu.KotlinLogging
import java.util.stream.Stream

object MonitoringTestDataProvider {
    private val logger = KotlinLogging.logger {}

    data class MonitoringTestData(
        val name: String,
        val url: String,
        val thrownException: Class<out Throwable>? = null
    ) {
        fun toMonitoring(): Monitoring {
            return Monitoring(id = null, name = this.name, url = this.url)
        }
    }

    fun validMonitoringItemProvider() = Stream.of(
        MonitoringTestData(name = "Google HTTP", url = "http://www.google.de"),
        MonitoringTestData(name = "Google HTTPS", url = "https://www.google.de"),
        MonitoringTestData(name = "Google TCP 80", url = "tcp://www.google.de:80"),
        MonitoringTestData(name = "Google ICMP", url = "icmp://www.google.de"),
    )

    fun invalidUrlMonitoringItemProvider() = Stream.of(
        MonitoringTestData(
            name = "Google TCP 80",
            url = "://",
            thrownException = Monitor.InvalidMonitoringFormatException::class.java
        ),
        MonitoringTestData(
            name = "Google TCP 80",
            url = "://www.google.de",
            thrownException = Monitor.InvalidMonitoringFormatException::class.java
        ),
        MonitoringTestData(
            name = "Google TCP 80",
            url = "invalid://",
            thrownException = Monitor.InvalidMonitoringFormatException::class.java
        ),
        MonitoringTestData(
            name = "Google TCP 80",
            url = "invalid://www.google.de",
            thrownException = Monitor.UnsupportedMonitoringProtocolException::class.java
        ),
        MonitoringTestData(
            name = "Google TCP 80",
            url = "http://",
            thrownException = Monitor.InvalidMonitoringFormatException::class.java
        ),
        MonitoringTestData(
            name = "Google TCP 80",
            url = "https://",
            thrownException = Monitor.InvalidMonitoringFormatException::class.java
        ),
        MonitoringTestData(
            name = "Google TCP 80",
            url = "tcp://",
            thrownException = Monitor.InvalidMonitoringFormatException::class.java
        ),
        MonitoringTestData(
            name = "Google TCP 80",
            url = "tcp://www.google.de",
            thrownException = MonitoringService.InvalidMonitoringFormatException::class.java
        ),
        MonitoringTestData(
            name = "Google TCP 80",
            url = "icmp://",
            thrownException = Monitor.InvalidMonitoringFormatException::class.java
        ),
    )
}