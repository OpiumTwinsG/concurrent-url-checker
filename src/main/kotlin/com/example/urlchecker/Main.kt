package com.example.urlchecker

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.math.roundToInt

fun main(args: Array<String>) =
    runBlocking {
        if (args.size < 3) {
            println("Usage: <file_with_urls> <maxConcurrent> <retryCount> [timeoutMs]")
            return@runBlocking
        }

        val filePath = args[0]
        val maxConcurrent = args[1].toIntOrNull() ?: error("maxConcurrent must be int")
        val retryCount = args[2].toIntOrNull() ?: error("retryCount must be int")
        val timeoutMs = args.getOrNull(3)?.toLongOrNull() ?: 5_000L

        val urls =
            File(filePath)
                .readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }

        println("Total URLs: ${urls.size}, concurrency=$maxConcurrent, retry=$retryCount")
        HttpClient(CIO) { engine { requestTimeout = timeoutMs } }.use { client ->
            val fetcher = KtorHeadFetcher(client)

            val manager =
                UrlCheckerManager(
                    urls = urls,
                    fetcher = fetcher,
                    maxConcurrent = maxConcurrent,
                    retryCount = retryCount,
                    timeoutMillis = timeoutMs,
                    showProgress = true,
                )
            val results = manager.runChecks()

            val out = "url_check_report.csv"
            generateCsvReport(results, out)
            println("Report saved to $out")

            val ok = results.count { it.httpStatus in 200..399 }
            val errHttp = results.count { it.httpStatus != null && it.httpStatus !in 200..399 }
            val failed = results.count { it.httpStatus == null }
            val avgMs = results.mapNotNull { it.responseTimeMs }.average()
            val maxMs = results.mapNotNull { it.responseTimeMs }.maxOrNull()

            println(
                "Summary: OK=$ok, HTTP_errors=$errHttp, Failed=$failed, " +
                    "avg=${avgMs.roundToInt()} ms, max=${maxMs ?: 0} ms",
            )
        }
    }
