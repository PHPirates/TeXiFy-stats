package nl.deltadak.texifystats

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import jetbrains.letsPlot.Stat
import jetbrains.letsPlot.geom.geomHistogram
import jetbrains.letsPlot.geom.geomLine
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.label.ggtitle
import jetbrains.letsPlot.scale.scaleXDateTime
import nl.deltadak.texifystats.api.getApolloClient
import nl.deltadak.texifystats.plots.PlotSize
import nl.deltadak.texifystats.plots.showPlot
import java.time.Instant

/** Actions on an issue. */
enum class Action { OPEN, CLOSE }

/** An issue was opened or closed. */
data class OpenCloseEvent(val time: Instant, val action: Action, val labels: List<String> = listOf())

typealias PlotFunction = (TotalIssuesStatistic, Pair<List<OpenCloseEvent>, List<OpenCloseEvent>>) -> Pair<String, Plot>

/**
 * Show total open issues over time, and total open pull requests over time.
 *
 * @param debug Whether to use all data available for the plot. If false, only one query will be done (faster), and plots will be shown small.
 * @param onlyOpenIssues Whether to show only open issues over time, or all issues (so total number of submitted issues).
 */
class TotalIssuesStatistic(private val githubToken: String, private val debug: Boolean = true, private val onlyOpenIssues: Boolean = true, private val takeLastEvents: Int? = null) {

    val repository = "TeXiFy-IDEA"
    val owner = "Hannah-Sten"

    // These are instance variables because data is received in batches and collected here
    private val issuesEventList = mutableListOf<OpenCloseEvent>()
    private val prEventList = mutableListOf<OpenCloseEvent>()

    // We have to remember the cursors to continue using them if the other type (issue, pull request) needs to continue paging
    private var issuesCursor: String? = null
    private var prCursor: String? = null

    /**
     * @return Issue and PR event lists.
     */
    // Unfortunately Edge and Edge1 are separate classes, so we can't abstract the foreach loop
    @Suppress("DuplicatedCode")
    fun receiveData(data: TotalIssuesQuery.Data, plotFunctions: List<PlotFunction>) {

        val issueEdges = data.repository?.issues?.edges ?: throw IllegalStateException("No data found")

        // Read open and close dates for issues
        issueEdges.forEach {
            val node = it?.node ?: return@forEach
            val createdAt = Instant.parse(node.createdAt.toString())
            val labels = node.labels?.edges?.mapNotNull { label -> label?.node?.name } ?: emptyList()
            issuesEventList.add(OpenCloseEvent(createdAt, Action.OPEN, labels))
            // Issues that are not closed are still open
            if (node.closedAt != null && onlyOpenIssues) {
                val closedAt = Instant.parse(node.closedAt.toString())
                issuesEventList.add(OpenCloseEvent(closedAt, Action.CLOSE, labels))
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
        if ((issueEdges.isEmpty() && prEdges.isNullOrEmpty()) || debug) {
            val plots = plotFunctions.map {
                it(this@TotalIssuesStatistic, Pair(issuesEventList, prEventList))
            }.toTypedArray()
            val titlesAndPlots = mapOf(*plots)
            showPlot(titlesAndPlots, if (debug) PlotSize.SMALL else PlotSize.LARGE)
        }
        else {
            println("Rate limit remaining: ${data.rateLimit?.remaining}")

            // Next page
            issuesCursor = if (issueEdges.isNotEmpty()) issueEdges.last()?.cursor else issuesCursor
            prCursor = if (prEdges?.isNotEmpty() == true) prEdges.last()?.cursor else prCursor
            runQuery(issuesCursor, prCursor, plotFunctions)
        }
    }

    /**
     * Run the GraphQL query, given an issues and a pull request cursor.
     *
     * When the query is received, if needed in the response handling a new query will be sent for the next page.
     */
    fun runQuery(issuesCursor: String? = null, pullRequestCursor: String? = null, plotFunctions: List<PlotFunction>) {
        val apolloClient = getApolloClient(githubToken)

        val query = TotalIssuesQuery(repository, owner, Input.fromNullable(issuesCursor), Input.fromNullable(pullRequestCursor), 100)

        apolloClient.query(query).enqueue(object : ApolloCall.Callback<TotalIssuesQuery.Data?>() {
            override fun onResponse(dataResponse: Response<TotalIssuesQuery.Data?>) {
                val data = dataResponse.data

                if (data == null) {
                    println("No data received")
                    println(dataResponse.errors)
                }
                else {
                    receiveData(data, plotFunctions)
                }
            }

            override fun onFailure(e: ApolloException) {
                println(e.message)
            }
        })
    }

    /**
     * @param type Used for the title.
     */
    fun createTotalIssuesPlot(list: List<OpenCloseEvent>, type: String = "issues"): Pair<String, Plot> {
        // Sort the events in order to walk through them and update the total issues counter for each event
        val eventList = list.toMutableList()
        eventList.sortBy { it.time }

        // List of number of open issues, at index i is the count right after the i'th event in eventList
        val totalIssuesList = mutableListOf<Int>()
        var counter = 0
        for (event in eventList) {
            if (event.action == Action.OPEN) {
                counter++
            }
            else {
                counter--
            }
            totalIssuesList.add(counter)
        }

        val n = takeLastEvents ?: eventList.size
        val plotData = mapOf<String, Any>(
                "date" to eventList.map { it.time.toEpochMilli() }.takeLast(n),
                "count" to totalIssuesList.takeLast(n)
        )

        val plot = ggplot(plotData) + geomLine { x = "date"; y = "count" } + scaleXDateTime() + ggtitle("Total open $type over time")

        return Pair("How many $type are open over time", plot)
    }

    fun showOpenedIssuesPerWeekPlot(eventList: List<OpenCloseEvent>, type: String): Pair<String, Plot> {
        val n = takeLastEvents ?: eventList.size
        val duplicates = mapOf<String, Any>(
                // Take last n before filtering, to ensure a fair view
                "date" to eventList.takeLast(n).filter { it.action == Action.OPEN && it.labels.contains("duplicate") }.map { it.time.toEpochMilli() }
        )

        val allIssues = mapOf<String, Any>(
                "date" to eventList.takeLast(n).filter { it.action == Action.OPEN }.map { it.time.toEpochMilli() }
        )

        // Note that when a bin width of a day is selected, takeLastEvents should be <= 500
        val binWidth = 1000.0 * 60 * 60 * 24 * 7 * 4
        val plot = ggplot(allIssues) +
                geomHistogram(data = allIssues, stat = Stat.bin(binWidth = binWidth), fill = "blue") { x = "date" } +
                geomHistogram(data = duplicates, stat = Stat.bin(binWidth = binWidth), fill = "red") { x = "date" } +
                scaleXDateTime() + ggtitle("New $type per 4 weeks: blue are all issues, red are issues that are a duplicate")

        return Pair("How many issues were opened per time window.", plot)
    }
}


fun main(args: Array<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("You need to provide the GitHub token")
    }

    val plotFunctions: List<PlotFunction> = listOf(
        { s, lists -> s.createTotalIssuesPlot(lists.first, "issues") },
        { s, lists -> s.createTotalIssuesPlot(lists.first, "pull requests") },
        { s, lists -> s.showOpenedIssuesPerWeekPlot(lists.first, "issues") },
    )
    TotalIssuesStatistic(args[0], debug = false, onlyOpenIssues = true, takeLastEvents = 1000).runQuery(plotFunctions = plotFunctions)
}
