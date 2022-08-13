package nl.deltadak.texifystats.api

import nl.deltadak.texifystats.ViewLoginQuery
import kotlin.system.exitProcess

suspend fun main(args: Array<String>) {
    val apolloClient = getApolloClient(args[0])

    val query = ViewLoginQuery()

    val dataResponse = apolloClient.query(query).execute()
    val data = dataResponse.data

    if (data == null) {
        println("No data received")
        println(dataResponse.errors)
    } else {
        println(dataResponse.data?.viewer?.login)
    }
    exitProcess(0)
}
