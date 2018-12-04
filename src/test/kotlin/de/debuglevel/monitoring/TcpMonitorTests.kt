package de.debuglevel.monitoring

import de.debuglevel.monitoring.monitors.TcpMonitor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TcpMonitorTests {
    @ParameterizedTest
    @MethodSource("validUrlsProvider")
    fun `validate valid URLs`(testData: UrlTestData) {
        // Arrange

        // Act
        val isValid = TcpMonitor().isValid(testData.value)

        //Assert
        assertThat(isValid).isEqualTo(true)
    }

    fun validUrlsProvider() = Stream.of(
            UrlTestData(value = "tcp://google.de:0"),
            UrlTestData(value = "tcp://google.de:1"),
            UrlTestData(value = "tcp://google.de:80"),
            UrlTestData(value = "tcp://google.de:65535"),
            UrlTestData(value = "tcp://google.customtld:80"),
            UrlTestData(value = "tcp://www.google.de:80"),
            UrlTestData(value = "tcp://192.168.0.1:80"),
            UrlTestData(value = "tcp://[2001:0db8:85a3:08d3::0370:7344]:8080")
    )

    @ParameterizedTest
    @MethodSource("invalidUrlsProvider")
    fun `validate invalid URLs`(testData: UrlTestData) {
        // Arrange

        // Act
        val isValid = TcpMonitor().isValid(testData.value)

        //Assert
        assertThat(isValid).isEqualTo(false)
    }

    fun invalidUrlsProvider() = Stream.of(
            UrlTestData(value = "tcp://"),
            UrlTestData(value = "tcp://:0"),
            UrlTestData(value = "tcp://google.de"),
            UrlTestData(value = "tcp://google.de:-1"),
            UrlTestData(value = "tcp://google.de:-2"),
            UrlTestData(value = "tcp://google.de:65536"),
            UrlTestData(value = "tcp://[2001:0db8:85a3:08d3::0370:7344]"),
            UrlTestData(value = "tcp://?"),
            UrlTestData(value = "tcp://??"),
            UrlTestData(value = "tcp://??/"),
            UrlTestData(value = "tcp://#"),
            UrlTestData(value = "tcp://##"),
            UrlTestData(value = "tcp://##/"),
            UrlTestData(value = "//"),
            UrlTestData(value = "//a"),
            UrlTestData(value = "///a"),
            UrlTestData(value = "///"),
            UrlTestData(value = "tcp:///a"),
            UrlTestData(value = "foo.com"),
            UrlTestData(value = "rdar://1234"),
            UrlTestData(value = "h://test"),
            UrlTestData(value = ":// should fail")
    )

    data class UrlTestData(
            val value: String
    )
}