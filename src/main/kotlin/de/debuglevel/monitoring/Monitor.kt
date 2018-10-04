package de.debuglevel.monitoring

interface Monitor {
    fun check(monitoring: Monitoring): Boolean
}