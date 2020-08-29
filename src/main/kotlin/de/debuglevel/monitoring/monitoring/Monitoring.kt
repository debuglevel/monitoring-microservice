package de.debuglevel.monitoring.monitoring

import de.debuglevel.monitoring.ServiceState
import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Monitoring(
    @Id
    @GeneratedValue
    val id: Int?,
    var name: String? = "",
    var url: String,
    /**
     * The IP of the resolved hostname
     */
    var ip: String? = null,
    /**
     * Last known ServiceState of the service
     */
    var serviceState: ServiceState = ServiceState.Unknown,
    /**
     * When the service was last in the "up" serviceState
     */
    var lastSeen: LocalDateTime? = null,
    /**
     * When the service was checked the last time
     */
    var lastCheck: LocalDateTime? = null,
    @DateCreated
    var createdOn: LocalDateTime = LocalDateTime.now(),
    @DateUpdated
    var lastModified: LocalDateTime = LocalDateTime.now()
)