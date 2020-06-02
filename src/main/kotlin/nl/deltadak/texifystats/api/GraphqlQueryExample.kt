package nl.deltadak.texifystats.api

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import nl.deltadak.texifystats.ViewLoginQuery
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    val apolloClient = getApolloClient(args[0])

    val query = ViewLoginQuery()

    apolloClient.query(query).enqueue(object : ApolloCall.Callback<ViewLoginQuery.Data?>() {
        override fun onResponse(dataResponse: Response<ViewLoginQuery.Data?>) {
            val data = dataResponse.data

            if (data == null) {
                println("No data received")
                println(dataResponse.errors)
            } else {
                println(dataResponse.data?.viewer?.login)
            }
            exitProcess(0)
        }

        override fun onFailure(e: ApolloException) {
            println(e.message)
        }
    })
}
