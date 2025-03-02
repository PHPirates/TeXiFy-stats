package nl.deltadak.texifystats.plots

import javafx.application.Platform
import org.jetbrains.letsPlot.commons.registration.Disposable
import org.jetbrains.letsPlot.core.util.MonolithicCommon
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.jfx.plot.component.DefaultPlotPanelJfx
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.Toolkit
import javax.swing.*
import javax.swing.JFrame.EXIT_ON_CLOSE

enum class PlotSize { SMALL, LARGE }

/**
 * Adapted from https://github.com/JetBrains/lets-plot-kotlin/blob/2cc96fd3ce258a61edbb5e741a8fc9ab4ad863d1/demo/jvm-javafx/src/main/kotlin/minimalDemo/Main.kt
 */
fun showPlot(
    plots: Map<String, Plot>,
    windowSize: PlotSize = PlotSize.LARGE,
) {
    val selectedPlotKey = plots.keys.first()
    val controller =
        Controller(
            plots,
            selectedPlotKey,
            false,
        )

    val window = JFrame("Example App")
    window.defaultCloseOperation = EXIT_ON_CLOSE
    window.contentPane.layout = BoxLayout(window.contentPane, BoxLayout.Y_AXIS)

    // Add controls
    val controlsPanel =
        Box.createHorizontalBox().apply {
            // Plot selector
            val plotButtonGroup = ButtonGroup()
            for (key in plots.keys) {
                plotButtonGroup.add(
                    JRadioButton(key, key == selectedPlotKey).apply {
                        addActionListener {
                            controller.plotKey = this.text
                        }
                    },
                )
            }

            this.add(
                Box.createHorizontalBox().apply {
                    border = BorderFactory.createTitledBorder("Plot")
                    for (elem in plotButtonGroup.elements) {
                        add(elem)
                    }
                },
            )

            // Preserve aspect ratio selector
            val aspectRadioButtonGroup = ButtonGroup()
            aspectRadioButtonGroup.add(
                JRadioButton("Original", false).apply {
                    addActionListener {
                        controller.preserveAspectRadio = true
                    }
                },
            )
            aspectRadioButtonGroup.add(
                JRadioButton("Fit container", true).apply {
                    addActionListener {
                        controller.preserveAspectRadio = false
                    }
                },
            )

            this.add(
                Box.createHorizontalBox().apply {
                    border = BorderFactory.createTitledBorder("Aspect ratio")
                    for (elem in aspectRadioButtonGroup.elements) {
                        add(elem)
                    }
                },
            )
        }
    window.contentPane.add(controlsPanel)

    // Add plot panel
    val plotContainerPanel = JPanel(GridLayout())
    window.contentPane.add(plotContainerPanel)

    controller.plotContainerPanel = plotContainerPanel
    controller.rebuildPlotComponent()

    val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize
    val width = screenSize.getWidth()
    val height = screenSize.getHeight()

    val plotSize =
        if (windowSize == PlotSize.SMALL) {
            Dimension(600, 300)
        } else {
            Dimension(width.toInt() - 70, height.toInt() - 70)
        }

    SwingUtilities.invokeLater {
        window.pack()
        window.size = plotSize
        window.setLocationRelativeTo(null)
        window.isVisible = true
    }
}

private class Controller(
    private val plots: Map<String, Plot>,
    initialPlotKey: String,
    initialPreserveAspectRadio: Boolean,
) {
    var plotContainerPanel: JPanel? = null
    var plotKey: String = initialPlotKey
        set(value) {
            field = value
            rebuildPlotComponent()
        }
    var preserveAspectRadio: Boolean = initialPreserveAspectRadio
        set(value) {
            field = value
            rebuildPlotComponent()
        }

    fun rebuildPlotComponent() {
        plotContainerPanel?.let {
            val container = plotContainerPanel!!
            // cleanup
            for (component in container.components) {
                if (component is Disposable) {
                    component.dispose()
                }
            }
            container.removeAll()

            // build
            container.add(createPlotPanel())
            container.parent?.revalidate()
        }
    }

    fun createPlotPanel(): JPanel {
        // Make sure JavaFX event thread won't get killed after JFXPanel is destroyed.
        Platform.setImplicitExit(false)

        val rawSpec = plots[plotKey]!!.toSpec()
        val processedSpec = MonolithicCommon.processRawSpecs(rawSpec, frontendOnly = false)

        return DefaultPlotPanelJfx(
            processedSpec = processedSpec,
            preserveAspectRatio = preserveAspectRadio,
            preferredSizeFromPlot = false,
            repaintDelay = 10,
        ) { messages ->
            for (message in messages) {
                println("[Example App] $message")
            }
        }
    }
}
