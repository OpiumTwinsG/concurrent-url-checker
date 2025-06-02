package com.example.urlchecker

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import java.io.File
import java.time.Instant
import kotlin.system.measureTimeMillis

class UrlCheckerManager(
    private val urls: List<String>,
    private val fetcher: HttpHeadFetcher,
    private val maxConcurrent: Int,
    private val retryCount: Int = 1,
    private val timeoutMillis: Long = 5_000L,
    private val showProgress: Boolean = true,
    private val csvPath: String = "url_check_report.csv",
    private val errorLogPath: String = "errors.log",
) {
    private val client =
        HttpClient(CIO) {
            engine { requestTimeout = timeoutMillis }
        }

    suspend fun runChecks(): List<UrlCheckTask> =
        coroutineScope {
            val sem = Semaphore(maxConcurrent)
            val progress = if (showProgress) ProgressBar(urls.size) else null
            val tasks = urls.map { UrlCheckTask(it) }

            tasks
                .map { task ->
                    launch(Dispatchers.IO) {
                        sem.withPermit { checkSingleUrl(task) }
                        progress?.tick()
                    }
                }.joinAll()

            (fetcher as? AutoCloseable)?.close()
            generateCsvReport(tasks, csvPath)
            (fetcher as? AutoCloseable)?.close()
            tasks
        }

    private suspend fun checkSingleUrl(task: UrlCheckTask) {
        var backoff = 1_000L
        repeat(retryCount) { attempt ->
            try {
                val elapsed =
                    measureTimeMillis {
                        val code = withTimeout(timeoutMillis) { fetcher.head(task.url) }
                        task.httpStatus = code
                    }
                task.status = Status.fromHttp(task.httpStatus)
                task.responseTimeMs = elapsed
                return
            } catch (e: Exception) {
                task.errorMessage = e.message
                if (attempt == retryCount - 1) logError(task)
                delay(backoff)
                backoff *= 2
            }
        }
    }

    private fun logError(task: UrlCheckTask) {
        File(errorLogPath)
            .appendText("${Instant.now()} | ${task.url} | ${task.errorMessage}\n")
    }
}
