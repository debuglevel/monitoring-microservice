package de.debuglevel.monitoring.monitors

import de.debuglevel.monitoring.Monitoring
import de.debuglevel.monitoring.ServiceState
import mu.KotlinLogging
import java.net.ConnectException
import java.net.Socket
import java.net.UnknownHostException

class TcpMonitor : Monitor {
    override fun isValid(monitoring: Monitoring): Boolean {
        if (monitoring.uri.port < 0 || monitoring.uri.port > 65535) {
            return false
        }

        return true
    }

    private val logger = KotlinLogging.logger {}

    override fun check(monitoring: Monitoring): ServiceState {
        val state = try {
            Socket(monitoring.uri.host, monitoring.uri.port).use {
                ServiceState.Up
            }
        } catch (e: UnknownHostException) {
            ServiceState.Down
        } catch (e: ConnectException) {
            ServiceState.Down
        } catch (e: Exception) {
            logger.warn { "Unhandled exception: $e" }
            ServiceState.Down
        }

        return state
    }
}