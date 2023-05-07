package nl.deltadak.texifystats

import nl.deltadak.texifystats.api.getApolloClient
import nl.deltadak.texifystats.plots.PlotSize
import nl.deltadak.texifystats.plots.showPlot
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.ggplot
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.scale.scaleXDateTime
import java.time.Instant

/**
 * Plot creation dates of open issues.
 * Will be shown as a histogram.
 */
class OpenIssuesByDatePlot(private val githubToken: String) {

    private fun receiveData(queryData: OpenIssuesQuery.Data) {
        val creationDatesList = queryData.repository?.issues?.nodes?.mapNotNull {
            Instant.parse(it?.createdAt.toString()).toEpochMilli()
        } ?: throw IllegalStateException("No creation dates present")

        val data = mapOf<String, Any>(
            "x" to creationDatesList,
        )

        val geom = geomHistogram(alpha = 0.3) {
            x = "x"
        }
        val p = ggplot(data) + geom + scaleXDateTime() + ggtitle("Creation dates of open issues")

        showPlot(mapOf("Open issues by date" to p), PlotSize.LARGE)
    }

    suspend fun runQuery() {
        val apolloClient = getApolloClient(githubToken)

        val query = OpenIssuesQuery("TeXiFy-IDEA", "Hannah-Sten")
        val dataResponse = apolloClient.query(query).execute()
        val data = dataResponse.data

        if (data == null) {
            println("No data received")
            println(dataResponse.errors)
        } else {
            receiveData(data)
        }
    }
}

suspend fun main(args: Array<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("You need to provide the GitHub token")
    }

    OpenIssuesByDatePlot(args[0]).runQuery()
}
