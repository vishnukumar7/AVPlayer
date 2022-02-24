package com.app.medialoader.data.url

import android.text.TextUtils
import com.app.medialoader.tinyhttpd.HttpHeaders
import com.app.medialoader.utils.LogUtil.d
import com.app.medialoader.utils.LogUtil.e
import com.app.medialoader.utils.Util
import com.app.medialoader.utils.Util.getMimeTypeFromUrl
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL

class DefaultUrlDataSource : BaseUrlDataSource {
    private var mUrlDataSourceInfo: UrlDataSourceInfo? = null
    private var mUrlConnection: HttpURLConnection? = null
    private var mInputStream: InputStream? = null

    constructor(url: String?) {
        val sourceInfo = mUrlDataSourceInfoCache[url]
        mUrlDataSourceInfo = sourceInfo
            ?: UrlDataSourceInfo(
                url!!, Int.MIN_VALUE.toLong(),
                getMimeTypeFromUrl(url)!!
            )
    }

    constructor(source: DefaultUrlDataSource) {
        mUrlDataSourceInfo = source.mUrlDataSourceInfo
        mUrlDataSourceInfoCache = source.mUrlDataSourceInfoCache
    }

    @Throws(IOException::class)
    override fun open(offset: Long) {
        try {
            d("Open connection " + (if (offset > 0) " with offset $offset" else "") + " to " + mUrlDataSourceInfo!!.url)
            mUrlConnection =
                URL(mUrlDataSourceInfo!!.url).openConnection(Proxy.NO_PROXY) as HttpURLConnection
            if (offset > 0) {
                mUrlConnection!!.setRequestProperty(
                    HttpHeaders.Names.RANGE,
                    "bytes=$offset-"
                )
            }
            val mime = mUrlConnection!!.contentType
            mInputStream =
                BufferedInputStream(mUrlConnection!!.inputStream, Util.DEFAULT_BUFFER_SIZE)
            val length =
                readSourceAvailableBytes(mUrlConnection, offset, mUrlConnection!!.responseCode)
            mUrlDataSourceInfo = UrlDataSourceInfo(mUrlDataSourceInfo!!.url, length, mime)
            mUrlDataSourceInfoCache[mUrlDataSourceInfo!!.url]=mUrlDataSourceInfo

        } catch (e: Exception) {
            throw IOException(
                "Error opening connection for " + mUrlDataSourceInfo!!.url + " with offset " + offset,
                e
            )
        }
    }

    @Throws(IOException::class)
    private fun readSourceAvailableBytes(
        connection: HttpURLConnection?,
        offset: Long,
        responseCode: Int
    ): Long {
        val contentLength = connection!!.contentLength
        return if (responseCode == HttpURLConnection.HTTP_OK) contentLength.toLong() else if (responseCode == HttpURLConnection.HTTP_PARTIAL) contentLength + offset else mUrlDataSourceInfo!!.length
    }

    @Synchronized
    @Throws(IOException::class)
    override fun length(): Long {
        if (mUrlDataSourceInfo!!.length == Int.MIN_VALUE.toLong()) {
            requestUrlDataSourceInfo()
        }
        return mUrlDataSourceInfo!!.length
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray?): Int {
        return try {
            mInputStream!!.read(buffer, 0, buffer!!.size)
        } catch (e: IOException) {
            throw IOException("Error reading data from " + mUrlDataSourceInfo!!.url, e)
        }
    }

    @Synchronized
    @Throws(IOException::class)
    override fun mimeType(): String? {
        if (TextUtils.isEmpty(mUrlDataSourceInfo!!.mimeType)) {
            requestUrlDataSourceInfo()
        }
        return mUrlDataSourceInfo!!.mimeType
    }

    @Throws(IOException::class)
    override fun close() {
        if (mInputStream != null) {
            mInputStream!!.close()
        }
        if (mUrlConnection != null) {
            mUrlConnection!!.disconnect()
        }
        mUrlDataSourceInfoCache.remove(mUrlDataSourceInfo!!.url)
    }

    @Throws(IOException::class)
    private fun requestUrlDataSourceInfo() {
        var urlConnection: HttpURLConnection? = null
        try {
            urlConnection = URL(mUrlDataSourceInfo!!.url).openConnection() as HttpURLConnection
            urlConnection.requestMethod = "HEAD"
            urlConnection.connectTimeout = 10000
            urlConnection.readTimeout = 10000
            val length = urlConnection.contentLength
            val mime = urlConnection.contentType
            mUrlDataSourceInfo = UrlDataSourceInfo(
                mUrlDataSourceInfo!!.url,
                length.toLong(), mime
            )
            mUrlDataSourceInfoCache.put(mUrlDataSourceInfo!!.url, mUrlDataSourceInfo)
            d("requestUrlDataSourceInfo: " + mUrlDataSourceInfo.toString())
        } catch (e: IOException) {
            e("Error request addHeader info from " + mUrlDataSourceInfo!!.url, e)
        } finally {
            urlConnection?.disconnect()
        }
    }

    override val url: String
        get() = mUrlDataSourceInfo!!.url
}