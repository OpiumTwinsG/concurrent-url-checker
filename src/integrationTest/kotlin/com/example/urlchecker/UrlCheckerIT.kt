package com.example.urlchecker

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UrlCheckerIT {
    private fun testClient() =
        HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    when (request.url.host) {
                        "ok.com" -> respond("", HttpStatusCode.OK)
                        "bad.com" -> respondError(HttpStatusCode.NotFound)
                        else -> error("Unhandled ${request.url}")
                    }
                }
            }
        }

    @Test
    fun `CSV и errors log создаются корректно`() =
        runTest {
            val tmp = Files.createTempDirectory("urlcheck")
            val csv = tmp.resolve("out.csv").toString()
            val elog = tmp.resolve("err.log").toString()

            val mgr =
                UrlCheckerManager(
                    urls = listOf("https://ok.com", "https://bad.com"),
                    fetcher = KtorHeadFetcher(testClient()),
                    maxConcurrent = 2,
                    csvPath = csv,
                    errorLogPath = elog,
                )

            mgr.runChecks()

            assertEquals(3, Files.readAllLines(Paths.get(csv)).size) // header+2
            assertTrue(Files.readString(Paths.get(elog)).contains("bad.com"))
        }
}
