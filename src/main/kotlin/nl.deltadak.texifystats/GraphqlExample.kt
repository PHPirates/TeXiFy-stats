package nl.deltadak.texifystats

import com.apollographql.apollo.ApolloClient

import okhttp3.OkHttpClient




fun main() {
    val BASE_URL = "https://api.github.com/graphql"

    val okHttpClient = OkHttpClient.Builder().build()

    val apolloClient = ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttpClient)
            .build()
}
