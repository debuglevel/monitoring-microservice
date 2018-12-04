package de.debuglevel.monitoring.rest

import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RestEndpointTests {

//    init {
//        val restEndpoint = RestEndpoint()
//        restEndpoint.start(arrayOf())
//
//        awaitInitialization()
//    }
//
//    @Test
//    fun `server listens on default port`() {
//        // Arrange
//
//        // Act
//        val response = ApiTestUtils.request("GET", "/greet/test", null)
//
//        // Assert
//        // HTTP Codes begin from "100". So something from 100 and above was probably a response to a HTTP request
//        assertThat(response?.status).isGreaterThanOrEqualTo(100)
//    }
//
//    @Nested
//    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
//    inner class `valid requests on greet` {
//        @ParameterizedTest
//        @MethodSource("validUrlsProvider")
//        fun `server sends greeting in body`(testData: UrlTestData) {
//            // Arrange
//
//            // Act
//            val response = ApiTestUtils.request("GET", "/greet/${testData.value}", null)
//
//            // Assert
//            assertThat(response?.body).contains(testData.expected)
//        }
//
//        @ParameterizedTest
//        @MethodSource("validUrlsProvider")
//        fun `server sends correct greeting on api version 2 and default`(testData: UrlTestData) {
//            // Arrange
//
//            // Act
//            val responseApiDefault = ApiTestUtils.request("GET", "/greet/${testData.value}", null)
//            val responseApiV2 = ApiTestUtils.request("GET", "/v2/greet/${testData.value}", null)
//
//            // Assert
//            assertThat(responseApiDefault?.body).contains(testData.expected)
//            assertThat(responseApiV2?.body).contains(testData.expected)
//        }
//
//        @ParameterizedTest
//        @MethodSource("validNameProviderApiV1")
//        fun `server sends correct greeting on api version 1`(testData: UrlTestData) {
//            // Arrange
//
//            // Act
//            val responseApiV1 = ApiTestUtils.request("GET", "/v1/greet/${testData.value}", null)
//
//            // Assert
//            assertThat(responseApiV1?.body).contains(testData.expected)
//        }
//
//        @ParameterizedTest
//        @MethodSource("validUrlsProvider")
//        fun `server sends status code 200`(testData: UrlTestData) {
//            // Arrange
//
//            // Act
//            val response = ApiTestUtils.request("GET", "/greet/${testData.value}", null)
//
//            // Assert
//            assertThat(response?.status).isEqualTo(200)
//        }
//
//        @ParameterizedTest
//        @MethodSource("validUrlsProvider")
//        fun `server sends json content type`(testData: UrlTestData) {
//            // Arrange
//
//            // Act
//            val response = ApiTestUtils.request("GET", "/greet/${testData.value}", null)
//
//            // Assert
//            assertThat(response?.contentType).isEqualTo("application/json")
//        }
//
//        @ParameterizedTest
//        @MethodSource("validUrlsProvider")
//        fun `server sends json content`(testData: UrlTestData) {
//            // Arrange
//
//            // Act
//            val response = ApiTestUtils.request("GET", "/greet/${testData.value}", null)
//
//            // Assert
//            val validJson = JsonUtils.isJSONValid(response?.body)
//            assertThat(validJson).isTrue()
//        }
//
//        fun validUrlsProvider() = Stream.of(
//                UrlTestData(value = "Mozart", expected = "Hello, Mozart!"),
//                UrlTestData(value = "Amadeus", expected = "Hello, Amadeus!"),
//                // TODO: Umlauts do not work when executed as gradle task in Windows
////                UrlTestData(value = "H%C3%A4nschen", expected = "Hello, Hänschen!"),
//                UrlTestData(value = "Max%20Mustermann", expected = "Hello, Max Mustermann!")
//        )
//
//        fun validNameProviderApiV1() = Stream.of(
//                UrlTestData(value = "Mozart", expected = "Hello from API v1, Mozart!"),
//                UrlTestData(value = "Amadeus", expected = "Hello from API v1, Amadeus!"),
//                // TODO: Umlauts do not work when executed as gradle task in Windows
////                UrlTestData(value = "H%C3%A4nschen", expected = "Hello, Hänschen!"),
//                UrlTestData(value = "Max%20Mustermann", expected = "Hello from API v1, Max Mustermann!")
//        )
//    }
//
//    @Nested
//    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
//    inner class `invalid requests on greet` {
//        @ParameterizedTest
//        @MethodSource("invalidUrlsProvider")
//        fun `server does not send greeting in body`(testData: UrlTestData) {
//            // Arrange
//
//            // Act
//            val response = ApiTestUtils.request("GET", "/greet/${testData.value}", null)
//
//            // Assert
//            assertThat(response?.body).doesNotContain("Hello, ${testData.value}!")
//        }
//
//        @ParameterizedTest
//        @MethodSource("invalidUrlsProvider")
//        fun `server sends error message`(testData: UrlTestData) {
//            // Arrange
//
//            // Act
//            val response = ApiTestUtils.request("GET", "/greet/${testData.value}", null)
//
//            // Assert
//            assertThat(response?.body).contains("message")
//        }
//
//        @ParameterizedTest
//        @MethodSource("invalidUrlsProvider")
//        fun `server sends status code 400`(testData: UrlTestData) {
//            // Arrange
//
//            // Act
//            val response = ApiTestUtils.request("GET", "/greet/${testData.value}", null)
//
//            // Assert
//            assertThat(response?.status).isEqualTo(400)
//        }
//
//        @ParameterizedTest
//        @MethodSource("invalidUrlsProvider")
//        fun `server sends json content type`(testData: UrlTestData) {
//            // Arrange
//
//            // Act
//            val response = ApiTestUtils.request("GET", "/greet/${testData.value}", null)
//
//            // Assert
//            assertThat(response?.contentType).isEqualTo("application/json")
//        }
//
//        @ParameterizedTest
//        @MethodSource("invalidUrlsProvider")
//        fun `server sends json content`(testData: UrlTestData) {
//            // Arrange
//
//            // Act
//            val response = ApiTestUtils.request("GET", "/greet/${testData.value}", null)
//
//            // Assert
//            val validJson = JsonUtils.isJSONValid(response?.body)
//            assertThat(validJson).isTrue()
//        }
//
//        fun invalidUrlsProvider() = Stream.of(
//                //UrlTestData(value = ""),
//                UrlTestData(value = "%20")
//        )
//    }
//
//    data class UrlTestData(
//            val value: String,
//            val expected: String? = null
//    )
}