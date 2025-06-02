package com.example.urlchecker

import java.util.concurrent.atomic.AtomicInteger

/**
 * Потокобезопасный progress-bar. Выводит строку вида
 * [██████░░░░░░░░░░░░░░░░░] 40 %
 */
object ProgressBar {
    private var last = -1
    private val printed = AtomicInteger(0)

    fun update(
        done: Int,
        total: Int,
    ) {
        val percent = done * 100 / total
        if (percent != last) {
            last = percent
            val barLen = 30
            val filled = percent * barLen / 100
            val bar = "#".repeat(filled) + ".".repeat(barLen - filled)
            print("\r[$bar] $percent%")
            if (done == total) println()
        }
    }
}
