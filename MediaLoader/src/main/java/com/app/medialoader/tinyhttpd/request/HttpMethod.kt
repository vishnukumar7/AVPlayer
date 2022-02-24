package com.app.medialoader.tinyhttpd.request

import java.io.IOException

enum class HttpMethod(private val method: String) {
    GET("GET"), POST("POST");

    override fun toString(): String {
        return method
    }

    companion object {
        @Throws(IOException::class)
        operator fun get(method: String): HttpMethod {
            if (method == GET.method) {
                return GET
            } else if (method == POST.method) {
                return POST
            }
            throw IOException("Unexpected method: $method")
        }
    }
}