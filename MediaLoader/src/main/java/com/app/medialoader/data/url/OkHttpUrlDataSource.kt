package com.app.medialoader.data.url

import java.io.IOException

/**
 * OkHttp实现，如果你想使用OkHttp代替默认的HttpURLConnection（不过android 4.4已经将okhttp默认为HttpUrlConnection的实现）
 * //TODO
 *
 * @author vincanyang
 */
class OkHttpUrlDataSource : BaseUrlDataSource() {
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