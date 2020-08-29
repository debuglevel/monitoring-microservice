package de.debuglevel.monitoring.monitoring

import de.debuglevel.monitoring.ServiceState
import java.net.URI
import java.time.LocalDateTime

data class Monitoring(val id: Int, var url: String, var name: String? = "") {
    /**
     * Last known serviceState of the service
     */
    var serviceState: ServiceState? = null

    /**
     * When the service was last in the "up" serviceState
     */
    var lastSeen: LocalDateTime? = null

    /**
     * When the service was checked the last time
     */
    var lastCheck: LocalDateTime? = null

    /**
     * The IP of the resolved hostname
     */
    var ip: String? = null

    /**
     * ID field for MongoDB
     */
    val _id
        get() = id

    /**
     * URI (generated from the "url" property)
     */
    @Transient
    val uri = URI(url)

    override fun toString() = url
}