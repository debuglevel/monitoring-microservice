package de.debuglevel.monitoring

import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class Monitoring (val id: Int, val url: String) {
    //@Transient
    var lastSeen: LocalDateTime? = null

    var lastCheckOkay: Boolean? = null

    val state: String
    get() {
        return when (lastCheckOkay)
        {
            true -> "[ UP ]"
            false -> "[DOWN]"
            else -> "[ ?? ]"
        }
    }

    @Transient
    val uri = URI(url)

    val lastSeenString: String
        get() {
            return lastSeen?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)) ?: "never"
        }

    override fun toString() = "$url"
}