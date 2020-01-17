package nl.deltadak.texifystats.plots

import jetbrains.datalore.base.datetime.Date
import jetbrains.datalore.base.datetime.DateTime
import jetbrains.datalore.base.datetime.Month
import jetbrains.datalore.base.datetime.tz.TimeZone
import jetbrains.letsPlot.geom.geom_line
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.scale.scale_x_datetime
import kotlin.random.Random

fun main() {

    val second = 1000.0
    val minute = 60.0 * second
    val hour = 60.0 * minute
    val day = 24.0 * hour

    val instant = TimeZone.UTC.toInstant(DateTime(Date(1, Month.FEBRUARY, 2003)))

    val nDays = 30
    val rnd = Random(0)

    val daysData = mapOf<String, Any>(
            "days" to (0..nDays).map { instant.timeSinceEpoch + it * day },
            "val" to (0..nDays).map { rnd.nextDouble(0.0, 20.0) }
    )

    val plot = ggplot(daysData) +
            geom_line { x = "days"; y = "val" } +
            scale_x_datetime()

    showPlot(plot)
}