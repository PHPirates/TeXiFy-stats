package nl.deltadak.texifystats.api

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import nl.deltadak.texifystats.ViewLoginQuery
import okhttp3.OkHttpClient


fun main(args: Array<String>) {
    val serverUrl = "https://api.github.com/graphql"

    val authHeader = args[0]

    // Add authentication header for GitHub
    val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                builder.header("Authorization", "Bearer $authHeader")
                chain.proceed(builder.build())
            }
            .build()

    val apolloClient = ApolloClient.builder()
            .serverUrl(serverUrl)
            .okHttpClient(okHttpClient)
            .build()

    val query = ViewLoginQuery()

    apolloClient.query(query).enqueue(object : ApolloCall.Callback<ViewLoginQuery.Data?>() {
        override fun onResponse(dataResponse: Response<ViewLoginQuery.Data?>) {
            val data = dataResponse.data()

            if (data == null) {
                println("No data received")
                println(dataResponse.errors())
            } else {
                println(dataResponse.data()?.viewer?.login)
            }
        }

        override fun onFailure(e: ApolloException) {
            println(e.message)
        }
    })
}
