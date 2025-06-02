package com.example.urlchecker

enum class Status {
    OK,
    HTTP_ERROR,
    FAILED,
    ;

    companion object {
        fun fromHttp(code: Int?): Status =
            when {
                code == null -> FAILED
                code in 200..399 -> OK
                else -> HTTP_ERROR
            }
    }
}
