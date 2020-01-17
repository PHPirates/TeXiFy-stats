package nl.deltadak.texifystats.plots

import javafx.application.Platform
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.plot.MonolithicAwt
import jetbrains.datalore.plot.builder.presentation.Style
import jetbrains.datalore.vis.demoUtils.jfx.SceneMapperJfxPanel
import jetbrains.datalore.vis.svg.SvgSvgElement
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.intern.toSpec
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

// Setup
val COMPONENT_FACTORY_JFX = { svg: SvgSvgElement -> SceneMapperJfxPanel(svg, listOf(Style.JFX_PLOT_STYLESHEET)) }

val EXECUTOR_JFX = { r: () -> Unit ->
    if (Platform.isFxApplicationThread()) {
        r.invoke()
    } else {
        Platform.runLater(r)
    }
}

/**
 * Adapted from https://github.com/JetBrains/lets-plot-kotlin/blob/b165c405be284fca5b8dece378a50879601bf12b/demo/jvm-javafx/src/main/kotlin/minimalDemo/Main.kt
 */
fun showPlot(plot: Plot) {
    SwingUtilities.invokeLater {

        // Create JFXPanel showing the plot.
        val plotSpec = plot.toSpec()
        val plotSize = DoubleVector(600.0, 300.0)

        val component =
                MonolithicAwt.buildPlotFromRawSpecs(plotSpec, plotSize, COMPONENT_FACTORY_JFX, EXECUTOR_JFX) {
                    for (message in it) {
                        println("PLOT MESSAGE: $message")
                    }
                }

        // Show plot in Swing frame.
        val frame = JFrame("Let's plot")
        frame.contentPane.add(component)
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.size = Dimension(plotSize.x.toInt() + 100, plotSize.y.toInt() + 100)
        frame.isVisible = true

    }
}