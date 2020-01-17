package nl.deltadak.texifystats.api

import com.apollographql.apollo.ApolloClient
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

    return ApolloClient.builder()
            .serverUrl(serverUrl)
            .okHttpClient(okHttpClient)
            .build()
}