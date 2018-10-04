package de.debuglevel.monitoring

import de.debuglevel.monitoring.rest.RestEndpoint

/**
 * Application entry point.
 *
 * Simply starts a RestEndpoint class
 *
 * @param args parameters from the command line call
 */
fun main(args: Array<String>) {
    RestEndpoint().start(args)
}