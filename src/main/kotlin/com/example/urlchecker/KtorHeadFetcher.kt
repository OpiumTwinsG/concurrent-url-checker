package com.example.urlchecker

import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.http.HttpMethod

class KtorHeadFetcher(
    private val client: HttpClient,
) : HttpHeadFetcher,
    AutoCloseable {
    override suspend fun head(url: String): Int = client.request(url) { method = HttpMethod.Head }.status.value

    override fun close() = client.close()
}
