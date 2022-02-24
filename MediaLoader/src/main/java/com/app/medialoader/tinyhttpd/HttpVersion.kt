package com.app.medialoader.tinyhttpd

import java.io.IOException

enum class HttpVersion(private var version: String) {

    HTTP_1_0("HTTP/1.0"),
    HTTP_1_1("HTTP/1.1"),
    HTTP_2("H2");

    companion object {

        @Throws(IOException::class)
        operator fun get(version: String): HttpVersion? {
            return when (version) {
                HTTP_1_0.version -> {
                    HTTP_1_0
                }
                HTTP_1_1.version -> {
                    HTTP_1_1
                }
                HTTP_2.version -> {
                    HTTP_2
                }
                else -> throw IOException("Unexpected version: $version")
            }
        }
    }

    override fun toString(): String {
        return version
    }

}