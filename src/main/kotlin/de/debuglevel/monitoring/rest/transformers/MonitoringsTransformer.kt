package de.debuglevel.monitoring.rest.transformers

import de.debuglevel.monitoring.Monitoring
import mu.KotlinLogging
import spark.ModelAndView
import spark.template.mustache.MustacheTemplateEngine
import java.util.*


object MonitoringsTransformer {
    private val logger = KotlinLogging.logger {}

    fun toPlaintext(monitorings: Set<Monitoring>): String {
        val model = HashMap<String, Any>()
        model["monitorings"] = monitorings.map { MonitoringViewModel(it) }

        return MustacheTemplateEngine().render(ModelAndView(model, "monitoring/list.plaintext.mustache"))
    }

    fun toHtml(monitorings: Set<Monitoring>): String {
        val model = HashMap<String, Any>()
        model["monitorings"] = monitorings.map { MonitoringViewModel(it) }

        return MustacheTemplateEngine().render(ModelAndView(model, "monitoring/list.html.mustache"))
    }
}