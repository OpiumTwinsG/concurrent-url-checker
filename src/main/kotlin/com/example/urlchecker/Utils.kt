package com.example.urlchecker

import java.io.File

fun generateCsvReport(
    tasks: List<UrlCheckTask>,
    outPath: String,
) {
    File(outPath).printWriter().use { w ->
        w.println("URL,Status,ResponseTimeMs,Error")
        tasks.forEach { t ->
            val status =
                when {
                    t.httpStatus in 200..399 -> "OK"
                    t.httpStatus != null -> "ERROR_${t.httpStatus}"
                    else -> "FAILED"
                }
            val time = t.responseTimeMs ?: ""
            val err = t.errorMessage?.replace(",", ";") ?: ""
            w.println("${t.url},$status,$time,$err")
        }
    }
}
