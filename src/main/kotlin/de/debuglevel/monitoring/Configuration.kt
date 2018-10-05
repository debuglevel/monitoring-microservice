package de.debuglevel.monitoring

import com.natpryce.konfig.*
import com.natpryce.konfig.Configuration
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import java.io.File

object Configuration {
    val configuration: Configuration

    init {
        var config: Configuration = systemProperties()

        config = config overriding
                EnvironmentVariables()

        config = config overriding
                ConfigurationProperties.fromOptionalFile(File("configuration.properties"))

        val defaultsPropertiesFilename = "defaults.properties"
        if (ClassLoader.getSystemClassLoader().getResource(defaultsPropertiesFilename) != null) {
            config = config overriding
                    ConfigurationProperties.fromResource(defaultsPropertiesFilename)
        }

        configuration = config
    }

//    val port = configuration.getOrNull(key_port) ?: configuration.getOrNull(key_server_port) ?: 8080

    val mongodbUrl = configuration.getOrNull(Key("mongodb.url", stringType)) ?: "localhost:27017"
}