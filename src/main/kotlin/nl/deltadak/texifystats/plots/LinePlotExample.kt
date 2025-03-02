package nl.deltadak.texifystats.plots

import org.jetbrains.letsPlot.commons.intern.datetime.Date
import org.jetbrains.letsPlot.commons.intern.datetime.DateTime
import org.jetbrains.letsPlot.commons.intern.datetime.Month
import org.jetbrains.letsPlot.commons.intern.datetime.tz.TimeZone
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.ggplot
import org.jetbrains.letsPlot.scale.scaleXDateTime
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
        "val" to (0..nDays).map { rnd.nextDouble(0.0, 20.0) },
    )

    val plot = ggplot(daysData) +
        geomLine { x = "days"; y = "val" } +
        scaleXDateTime()

    showPlot(mapOf("Line plot" to plot))
}
