package de.debuglevel.monitoring.monitoring

import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository

@Repository
interface MonitoringRepository : CrudRepository<Monitoring, Int>
{
    fun existsByUrl(url: String): Boolean
}