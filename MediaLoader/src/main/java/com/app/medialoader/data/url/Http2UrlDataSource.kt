package com.app.medialoader.data.url

import java.io.IOException

/**
 * Http2实现
 * //TODO
 *
 * @author vincanyang
 */
class Http2UrlDataSource : BaseUrlDataSource() {
    @Throws(IOException::class)
    override fun open(offset: Long) {
    }

    @Throws(IOException::class)
    override fun length(): Long {
        return 0
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray?): Int {
        return 0
    }

    @Throws(IOException::class)
    override fun mimeType(): String? {
        return null
    }

    override val url: String?
        get() = null

    @Throws(IOException::class)
    override fun close() {
    }
}