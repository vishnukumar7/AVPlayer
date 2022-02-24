package com.app.medialoader.data.url

import java.io.Closeable
import java.io.IOException

interface UrlDataSource : Closeable {
    @Throws(IOException::class)
    fun open(offset: Long)

    @Throws(IOException::class)
    fun length(): Long

    @Throws(IOException::class)
    fun read(buffer: ByteArray?): Int

    @Throws(IOException::class)
    fun mimeType(): String?
    val url: String?
}
