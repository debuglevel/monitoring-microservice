package de.debuglevel.monitoring.monitoring

import de.debuglevel.monitoring.ServiceState
import io.micronaut.test.annotation.MicronautTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.streams.toList

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MonitoringServiceTests {

    @Inject
    lateinit var service: MonitoringService

    @Inject
    lateinit var repository: MonitoringRepository

    @BeforeEach
    fun `clean repository`() {
        repository.deleteAll()
    }

    @ParameterizedTest
    @MethodSource("validMonitoringProvider")
    fun `add and get valid monitoring`(monitoringTestData: MonitoringTestDataProvider.MonitoringTestData) {
        // Arrange
        val monitoring = monitoringTestData.toMonitoring()

        // Act
        val addedItem = service.add(monitoring)
        val getItem = service.get(addedItem.id!!)

        // Assert
        Assertions.assertThat(addedItem.id).isNotNull
        Assertions.assertThat(getItem.id).isNotNull
        Assertions.assertThat(addedItem.url).isEqualTo(monitoring.url)
        Assertions.assertThat(getItem.url).isEqualTo(monitoring.url)
        Assertions.assertThat(addedItem.name).isEqualTo(monitoring.name)
        Assertions.assertThat(getItem.name).isEqualTo(monitoring.name)
    }

    fun validMonitoringProvider() = MonitoringTestDataProvider.validMonitoringItemProvider()

    @Test
    fun `add existing monitoring`() {
        // Arrange
        service.add(Monitoring(id = null, url = "http://www.existing.invalid", name = "Name"))
        val duplicateMonitoring = Monitoring(id = null, url = "http://www.existing.invalid", name = "Name")

        // Act & Assert
        assertThrows<MonitoringService.MonitoringAlreadyExistsException> { service.add(duplicateMonitoring) }
    }

    @ParameterizedTest
    @MethodSource("invalidUrlMonitoringProvider")
    fun `add invalid monitoring`(monitoringTestData: MonitoringTestDataProvider.MonitoringTestData) {
        // Arrange
        val monitoring = monitoringTestData.toMonitoring()

        // Act & Assert
        Assertions.assertThatThrownBy { service.add(monitoring) }.isInstanceOf(monitoringTestData.thrownException!!)
    }

    fun invalidUrlMonitoringProvider() = MonitoringTestDataProvider.invalidUrlMonitoringItemProvider()

    @Test
    fun `update monitoring`() {
        // Arrange
        val monitoring = Monitoring(
            id = null,
            url = "http://www.original.invalid",
            name = "Name original",
            ip = "111.111.111.111",
            lastSeen = LocalDateTime.MIN,
            serviceState = ServiceState.Down,
            lastCheck = LocalDateTime.MIN,
        )
        val addedItem = service.add(monitoring)
        val updateItem = addedItem.copy(
            url = "http://www.updated.invalid",
            name = "Name updated",
            ip = "222.222.222.22",
            lastSeen = LocalDateTime.MAX,
            serviceState = ServiceState.Up,
            lastCheck = LocalDateTime.MAX,
        )

        // Act
        val updatedItem = service.update(addedItem.id!!, updateItem)

        // Assert
        val getItem = service.get(addedItem.id!!)
        Assertions.assertThat(updatedItem.id).isEqualTo(addedItem.id)
        Assertions.assertThat(getItem.id).isEqualTo(addedItem.id)
        Assertions.assertThat(updatedItem.url).isEqualTo("http://www.updated.invalid")
        Assertions.assertThat(getItem.url).isEqualTo("http://www.updated.invalid")
        Assertions.assertThat(updatedItem.name).isEqualTo("Name updated")
        Assertions.assertThat(getItem.name).isEqualTo("Name updated")
        Assertions.assertThat(updatedItem.ip).isEqualTo("222.222.222.22")
        Assertions.assertThat(getItem.ip).isEqualTo("222.222.222.22")
        Assertions.assertThat(updatedItem.lastSeen).isEqualTo(LocalDateTime.MAX)
        Assertions.assertThat(getItem.lastSeen).isEqualTo(LocalDateTime.MAX)
        Assertions.assertThat(updatedItem.lastCheck).isEqualTo(LocalDateTime.MAX)
        Assertions.assertThat(getItem.lastCheck).isEqualTo(LocalDateTime.MAX)
        Assertions.assertThat(updatedItem.serviceState).isEqualTo(ServiceState.Up)
        Assertions.assertThat(getItem.serviceState).isEqualTo(ServiceState.Up)
        Assertions.assertThat(updatedItem.createdOn).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS))
        Assertions.assertThat(getItem.createdOn).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS))
        Assertions.assertThat(updatedItem.lastModified).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS))
        Assertions.assertThat(getItem.lastModified).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS))
    }

    @Test
    fun `update monitoring to existing URL`() {
        // Arrange
        service.add(Monitoring(id = null, url = "http://www.existing.invalid", name = "Name"))

        val monitoring = service.add(
            Monitoring(
                id = null,
                url = "http://www.new.invalid",
                name = "Name",
            )
        )
        val updateItem = monitoring.copy(url = "http://www.existing.invalid")

        // Act & Assert
        assertThrows<MonitoringService.MonitoringAlreadyExistsException> { service.update(monitoring.id!!, updateItem) }
    }

    @ParameterizedTest
    @MethodSource("invalidUrlMonitoringProvider")
    fun `update monitoring to invalid URL`(monitoringTestData: MonitoringTestDataProvider.MonitoringTestData) {
        // Arrange
        val monitoring = service.add(Monitoring(id = null, url = "http://www.monitoring.invalid", name = "Name"))
        val updateItem = monitoring.copy(url = monitoringTestData.url)

        // Act & Assert
        Assertions.assertThatThrownBy { service.update(monitoring.id!!, updateItem) }
            .isInstanceOf(monitoringTestData.thrownException!!)
    }

    @Test
    fun `delete monitoring`() {
        // Arrange
        val monitoring = service.add(Monitoring(id = null, url = "http://www.monitoring.invalid", name = "Name"))

        // Act
        service.delete(monitoring.id!!)

        // Assert
        Assertions.assertThat(service.list()).doesNotContain(monitoring)
        Assertions.assertThat(service.list()).noneMatch { it.id == monitoring.id }
        Assertions.assertThat(service.list()).noneMatch { it.url == monitoring.url }
        Assertions.assertThatThrownBy { service.get(monitoring.id!!) }
            .isInstanceOf(MonitoringService.MonitoringNotFoundException::class.java)
    }

    @Test
    fun list() {
        // Arrange
        val addedMonitorings = validMonitoringProvider().toList().map { service.add(it.toMonitoring()) }.toSet()

        // Act
        val listMonitorings = service.list()

        // Assert
        Assertions.assertThat(listMonitorings).containsExactlyInAnyOrder(*addedMonitorings.toTypedArray())
    }
}