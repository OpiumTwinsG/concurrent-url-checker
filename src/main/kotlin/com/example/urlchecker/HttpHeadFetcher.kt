package com.example.urlchecker

interface HttpHeadFetcher {
    suspend fun head(url: String): Int
}
