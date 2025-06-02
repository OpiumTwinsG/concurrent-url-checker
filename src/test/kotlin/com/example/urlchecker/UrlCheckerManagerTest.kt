package com.example.urlchecker

import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

interface HttpHeadFetcher {
    suspend fun head(url: String): Int
}

class KtorHeadFetcher(
    private val client: HttpClient,
) : HttpHeadFetcher {
    override suspend fun head(url: String): Int = client.head(url).status.value
}

class UrlCheckerManagerTest {
    private val fetcher = mockk<HttpHeadFetcher>()

    @Test
    fun `200 OK is mapped to Status_OK`() =
        runTest {
            coEvery { fetcher.head("https://good.com") } returns 200

            val mgr =
                UrlCheckerManager(
                    urls = listOf("https://good.com"),
                    fetcher = fetcher,
                    maxConcurrent = 1,
                )
            val result = mgr.runChecks()

            assertEquals(Status.OK, result.first().status)
            coVerify(exactly = 1) { fetcher.head(any()) }
        }
}
