package com.example.urlchecker

import java.util.concurrent.atomic.AtomicInteger

/**
 * Потокобезопасный progress-bar: [##########..........] 33 %
 */
class ProgressBar(
    private val total: Int,
    private val barWidth: Int = 30,
) {
    private val done = AtomicInteger(0)
    private var last = -1

    fun tick(value: Int = done.incrementAndGet()) = update(value, total)

    fun update(
        doneNow: Int,
        total: Int,
    ) {
        val percent = doneNow * 100 / total
        if (percent != last) {
            last = percent
            val filled = percent * barWidth / 100
            val bar = "#".repeat(filled) + ".".repeat(barWidth - filled)
            print("\r[$bar] $percent%")
            if (doneNow == total) println()
        }
    }
}
