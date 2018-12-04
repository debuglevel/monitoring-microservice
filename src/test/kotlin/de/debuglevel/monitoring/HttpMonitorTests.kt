package de.debuglevel.monitoring

import de.debuglevel.monitoring.monitors.HttpMonitor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpMonitorTests {
    @ParameterizedTest
    @MethodSource("validUrlsProvider")
    fun `validate valid URLs`(testData: UrlTestData) {
        // Arrange

        // Act
        val isValid = HttpMonitor().isValid(testData.value)

        //Assert
        assertThat(isValid).isEqualTo(true)
    }

    fun validUrlsProvider() = Stream.of(
            UrlTestData(value = "http://google.de"),
            UrlTestData(value = "https://google.de"),
            UrlTestData(value = "http://www.google.de"),
            UrlTestData(value = "https://www.google.de"),
            UrlTestData(value = "http://www.google.de:80"),
            UrlTestData(value = "https://www.google.de:80"),
            UrlTestData(value = "http://www.google.de:443"),
            UrlTestData(value = "https://www.google.de:443"),
            UrlTestData(value = "http://www.google.de:1"),
            UrlTestData(value = "https://www.google.de:1"),
            UrlTestData(value = "http://www.google.de:65535"),
            UrlTestData(value = "https://www.google.de:65535"),
            UrlTestData(value = "http://myshit.corp-network.de"),
            UrlTestData(value = "http://google.customtld"),
            UrlTestData(value = "https://google.customtld"),
            UrlTestData(value = "http://google.de/path/to/something"),
            UrlTestData(value = "http://google.de/path/to/something/"),
            UrlTestData(value = "http://google.de/path/to/something?key"),
            UrlTestData(value = "http://google.de/path/to/something?key=value"),
            UrlTestData(value = "http://google.de/path/to/something?key=value&key2=value2"),
            UrlTestData(value = "http://google.de/path/to/?key=value&key2=value2"),
            UrlTestData(value = "http://192.168.0.1"),
            UrlTestData(value = "http://192.168.0.1/"),
            UrlTestData(value = "http://192.168.0.1:80"),
            UrlTestData(value = "http://192.168.0.1:80/"),
            UrlTestData(value = "http://[2001:0db8:85a3:08d3::0370:7344]:8080/"),
            UrlTestData(value = "http://[2001:0db8:85a3:08d3::0370:7344]/"),
            UrlTestData(value = "https://[2001:0db8:85a3:08d3::0370:7344]/"),
            UrlTestData(value = "http://foo.com/blah_blah"),
            UrlTestData(value = "http://foo.com/blah_blah/"),
            UrlTestData(value = "http://foo.com/blah_blah_(wikipedia)"),
            UrlTestData(value = "http://foo.com/blah_blah_(wikipedia)_(again)"),
            UrlTestData(value = "http://www.example.com/wpstyle/?p=364"),
            UrlTestData(value = "https://www.example.com/foo/?bar=baz&inga=42&quux"),
            UrlTestData(value = "http://✪df.ws/123"),
            UrlTestData(value = "http://userid:password@example.com:8080"),
            UrlTestData(value = "http://userid:password@example.com:8080/"),
            UrlTestData(value = "http://userid@example.com"),
            UrlTestData(value = "http://userid@example.com/"),
            UrlTestData(value = "http://userid@example.com:8080"),
            UrlTestData(value = "http://userid@example.com:8080/"),
            UrlTestData(value = "http://userid:password@example.com"),
            UrlTestData(value = "http://userid:password@example.com/"),
            UrlTestData(value = "http://➡.ws/䨹"),
            UrlTestData(value = "http://⌘.ws"),
            UrlTestData(value = "http://⌘.ws/"),
            UrlTestData(value = "http://foo.com/blah_(wikipedia)#cite-1"),
            UrlTestData(value = "http://foo.com/blah_(wikipedia)_blah#cite-1"),
            UrlTestData(value = "http://foo.com/unicode_(✪)_in_parens"),
            UrlTestData(value = "http://foo.com/(something)?after=parens"),
            UrlTestData(value = "http://☺.damowmow.com/"),
            UrlTestData(value = "http://code.google.com/events/#&product=browser"),
            UrlTestData(value = "http://j.mp"),
            UrlTestData(value = "http://foo.bar/?q=Test%20URL-encoded%20stuff"),
            UrlTestData(value = "http://مثال.إختبار"),
            UrlTestData(value = "http://例子.测试"),
            UrlTestData(value = "http://-.~_!$&'()*+,;=:%40:80%2f::::::@example.com"),
            UrlTestData(value = "http://1337.net"),
            UrlTestData(value = "http://a.b-c.de"),
            UrlTestData(value = "http://223.255.255.25")
    )

    @ParameterizedTest
    @MethodSource("invalidUrlsProvider")
    fun `validate invalid URLs`(testData: UrlTestData) {
        // Arrange

        // Act
        val isValid = HttpMonitor().isValid(testData.value)

        //Assert
        assertThat(isValid).isEqualTo(false)
    }

    fun invalidUrlsProvider() = Stream.of(
            UrlTestData(value = "http://"),
//            UrlTestData(value = "http://."),
//            UrlTestData(value = "http://.."),
//            UrlTestData(value = "http://../"),
            UrlTestData(value = "http://?"),
            UrlTestData(value = "http://??"),
            UrlTestData(value = "http://??/"),
            UrlTestData(value = "http://#"),
            UrlTestData(value = "http://##"),
            UrlTestData(value = "http://##/"),
//            UrlTestData(value = "http://foo.bar?q=Spaces should be encoded"),
            UrlTestData(value = "//"),
            UrlTestData(value = "//a"),
            UrlTestData(value = "///a"),
            UrlTestData(value = "///"),
            UrlTestData(value = "http:///a"),
            UrlTestData(value = "foo.com"),
            UrlTestData(value = "rdar://1234"),
            UrlTestData(value = "h://test"),
//            UrlTestData(value = "http:// shouldfail.com"),
            UrlTestData(value = ":// should fail"),
//            UrlTestData(value = "http://foo.bar/foo(bar)baz quux"),
            UrlTestData(value = "ftps://foo.bar/"),
//            UrlTestData(value = "http://-error-.invalid/"),
//            UrlTestData(value = "http://-a.b.co"),
//            UrlTestData(value = "http://a.b-.co"),
//            UrlTestData(value = "http://0.0.0.0"),
//            UrlTestData(value = "http://3628126748"),
//            UrlTestData(value = "http://.www.foo.bar/"),
//            UrlTestData(value = "http://www.foo.bar./"),
            UrlTestData(value = "http://www.google.de:65536"),
            UrlTestData(value = "https://www.google.de:65536"),
            UrlTestData(value = "http://www.google.de:-2"),
            UrlTestData(value = "https://www.google.de:-2")
//            UrlTestData(value = "http://.www.foo.bar./")
    )

    data class UrlTestData(
            val value: String
    )
}