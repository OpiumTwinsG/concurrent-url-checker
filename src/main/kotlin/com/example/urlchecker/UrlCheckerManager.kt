package com.example.urlchecker

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.system.measureTimeMillis

class UrlCheckerManager(
    private val urls: List<String>,
    private val maxConcurrent: Int,
    private val retryCount: Int = 1,
    private val timeoutMillis: Long = 5_000L,
) {
    private val client =
        HttpClient(CIO) {
            engine { requestTimeout = timeoutMillis }
        }

    suspend fun runChecks(): List<UrlCheckTask> =
        coroutineScope {
            val sem = Semaphore(maxConcurrent)
            val tasks = urls.map { UrlCheckTask(it) }

            tasks
                .map { task ->
                    launch(Dispatchers.IO) { sem.withPermit { checkSingleUrl(task) } }
                }.joinAll()

            client.close()
            tasks
        }

    private suspend fun checkSingleUrl(task: UrlCheckTask) {
        repeat(retryCount) { attempt ->
            try {
                val elapsed =
                    measureTimeMillis {
                        val resp: HttpResponse = client.request(task.url) { method = HttpMethod.Head }
                        task.httpStatus = resp.status.value
                    }
                task.responseTimeMs = elapsed
                return
            } catch (e: Exception) {
                task.errorMessage = e.message
                if (attempt == retryCount - 1) logError(task)
                delay(500)
            }
        }
    }

    private fun logError(task: UrlCheckTask) {
        java.io
            .File("errors.log")
            .appendText("${java.time.Instant.now()} | ${task.url} | ${task.errorMessage}\n")
    }
}
