package nl.deltadak.texifystats.api

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.DefaultHttpRequestComposer
import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.apollographql.apollo3.network.http.HttpNetworkTransport
import okhttp3.OkHttpClient

fun getApolloClient(authHeader: String): ApolloClient {
    val serverUrl = "https://api.github.com/graphql"

    // Add authentication header for GitHub
    val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                builder.header("Authorization", "Bearer $authHeader")
                chain.proceed(builder.build())
            }
            .build()

    return ApolloClient(
        networkTransport = HttpNetworkTransport(
            httpRequestComposer = DefaultHttpRequestComposer(serverUrl),
            engine=DefaultHttpEngine(okHttpClient=okHttpClient)
        )
    )
}