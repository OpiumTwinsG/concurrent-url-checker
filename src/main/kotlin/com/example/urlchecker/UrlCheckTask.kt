package com.example.urlchecker

data class UrlCheckTask(
    val url: String,
    var httpStatus: Int? = null,
    var status: Status = Status.FAILED,
    var responseTimeMs: Long? = null,
    var errorMessage: String? = null,
)
