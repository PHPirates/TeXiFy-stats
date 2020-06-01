package nl.deltadak.texifystats

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import jetbrains.letsPlot.geom.geom_line
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.ggtitle
import jetbrains.letsPlot.scale.scale_x_datetime
import nl.deltadak.texifystats.api.getApolloClient
import nl.deltadak.texifystats.plots.PlotSize
import nl.deltadak.texifystats.plots.showPlot
import java.time.Instant

/** Actions on an issue. */
enum class Action { OPEN, CLOSE }

/** An issue was opened or closed. */
data class OpenCloseEvent(val time: Instant, val action: Action)

/**
 * Show total open issues over time, and total open pull requests over time.
 *
 * @param useAllData Whether to use all data available for the plot. If false, only one query will be done (faster).
 * @param onlyOpenIssues Whether to show only open issues over time, or all issues (so total number of submitted issues).
 */
class TotalIssuesStatistic(private val githubToken: String, private val useAllData: Boolean = true, private val onlyOpenIssues: Boolean = true) {

    private val issuesEventList = mutableListOf<OpenCloseEvent>()
    private val prEventList = mutableListOf<OpenCloseEvent>()

    // We have to remember the cursors to continue using them if the other type (issue, pull request) needs to continue paging
    private var issuesCursor: String? = null
    private var prCursor: String? = null

    // Unfortunately Edge and Edge1 are separate classes, so we can't abstract the foreach loop
    @Suppress("DuplicatedCode")
    fun receiveData(data: TotalIssuesQuery.Data) {

        val issueEdges = data.repository?.issues?.edges ?: return

        // Read open and close dates for issues
        issueEdges.forEach {
            val node = it?.node ?: return@forEach
            val createdAt = Instant.parse(node.createdAt.toString())
            issuesEventList.add(OpenCloseEvent(createdAt, Action.OPEN))
            // Issues that are not closed are still open
            if (node.closedAt != null && onlyOpenIssues) {
                val closedAt = Instant.parse(node.closedAt.toString())
                issuesEventList.add(OpenCloseEvent(closedAt, Action.CLOSE))
            }
        }

        val prEdges = data.repository.pullRequests.edges

        // Same for pull requests
        prEdges?.forEach {
            prEventList.add(OpenCloseEvent(Instant.parse(it?.node?.createdAt.toString()), Action.OPEN))
            if (it?.node?.closedAt != null && onlyOpenIssues) {
                prEventList.add(OpenCloseEvent(Instant.parse(it.node.closedAt.toString()), Action.CLOSE))
            }
        }

        // If we have paginated to the end for both issues and pull requests
        if ((issueEdges.isNullOrEmpty() && prEdges.isNullOrEmpty()) || !useAllData) {
            createPlot(issuesEventList, "issues")
            createPlot(prEventList, "pull requests")
        }
        else {
            println("Rate limit remaining: ${data.rateLimit?.remaining}")

            // Next page
            issuesCursor = if (issueEdges.isNotEmpty()) issueEdges.last()?.cursor else issuesCursor
            prCursor = if (prEdges?.isNotEmpty() == true) prEdges.last()?.cursor else prCursor
            runQuery(issuesCursor, prCursor)
        }
    }

    /**
     * @param type Used for the title.
     */
    private fun createPlot(eventList: MutableList<OpenCloseEvent>, type: String = "issues") {
        // Sort the events in order to walk through them and update the total issues counter for each event
        eventList.sortBy { it.time }

        // List of number of open issues, at index i is the count right after the i'th event in eventList
        val totalIssuesList = mutableListOf<Int>()
        var counter = 0
        for (event in eventList) {
            if (event.action == Action.OPEN) {
                counter++
            } else {
                counter--
            }
            totalIssuesList.add(counter)
        }

        val plotData = mapOf<String, Any>(
                "date" to eventList.map { it.time.toEpochMilli() },
                "count" to totalIssuesList
        )

        val plot = ggplot(plotData) + geom_line { x = "date"; y = "count" } + scale_x_datetime() + ggtitle("Total open $type over time")

        showPlot(plot, PlotSize.LARGE)
    }

    /**
     * Run the GraphQL query, given an issues and a pull request cursor.
     *
     * When the query is received, if needed in the response handling a new query will be sent for the next page.
     */
    fun runQuery(issuesCursor: String? = null, pullRequestCursor: String? = null) {
        val apolloClient = getApolloClient(githubToken)

        val query = TotalIssuesQuery("TeXiFy-IDEA", "Hannah-Sten", Input.fromNullable(issuesCursor), Input.fromNullable(pullRequestCursor), 100)

        apolloClient.query(query).enqueue(object : ApolloCall.Callback<TotalIssuesQuery.Data?>() {
            override fun onResponse(dataResponse: Response<TotalIssuesQuery.Data?>) {
                val data = dataResponse.data

                if (data == null) {
                    println("No data received")
                    println(dataResponse.errors)
                } else {
                    receiveData(data)
                }
            }

            override fun onFailure(e: ApolloException) {
                println(e.message)
            }
        })
    }
}


fun main(args: Array<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("You need to provide the GitHub token")
    }

    TotalIssuesStatistic(args[0], useAllData = true, onlyOpenIssues = false).runQuery()
}
