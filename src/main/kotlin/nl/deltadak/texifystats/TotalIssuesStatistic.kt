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
import nl.deltadak.texifystats.plots.showPlot
import java.time.Instant

/** Actions on an issue. */
enum class Action { OPEN, CLOSE }

/** An issue was opened or closed. */
data class OpenCloseEvent(val time: Instant, val action: Action)


fun createTotalIssuesPlot(data: ViewIssuesCountQuery.Data) {
    val eventList = mutableListOf<OpenCloseEvent>()

    // Read open and close dates
    data.repository?.issues?.nodes?.forEach {
        val createdAt = Instant.parse(it?.createdAt.toString())
        eventList.add(OpenCloseEvent(createdAt, Action.OPEN))
        // Issues that are not closed are still open
        if (it?.closedAt != null) {
            val closedAt = Instant.parse(it.closedAt.toString())
            eventList.add(OpenCloseEvent(closedAt, Action.CLOSE))
        }
    }

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

    val plot = ggplot(plotData) + geom_line { x = "date"; y = "count" } + scale_x_datetime() + ggtitle("Total open issues over time")

    showPlot(plot)
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("You need to provide the GitHub token")
    }

    val apolloClient = getApolloClient(args[0])

    val query = ViewIssuesCountQuery("TeXiFy-IDEA", "Hannah-Sten", Input.fromNullable(null))

    apolloClient.query(query).enqueue(object : ApolloCall.Callback<ViewIssuesCountQuery.Data?>() {
        override fun onResponse(dataResponse: Response<ViewIssuesCountQuery.Data?>) {
            val data = dataResponse.data()

            if (data == null) {
                println("No data received")
                println(dataResponse.errors())
            } else {
                createTotalIssuesPlot(data)
            }
        }

        override fun onFailure(e: ApolloException) {
            println(e.message)
        }
    })
}
