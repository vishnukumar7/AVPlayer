package com.app.medialoader.tinyhttpd.response

import com.app.medialoader.tinyhttpd.HttpHeaders
import com.app.medialoader.tinyhttpd.response.HttpStatus
import com.app.medialoader.tinyhttpd.HttpVersion
import java.io.IOException
import kotlin.Throws

/**
 * 响应接口
 *
 * @author vincanyang
 */
interface Response {
    fun status(): HttpStatus
    fun setStatus(status: HttpStatus)
    fun protocol(): HttpVersion
    fun headers(): HttpHeaders

    /**
     * 添加头部
     *
     * @param key   [com.app.medialoader.tinyhttpd.HttpHeaders.Names]
     * @param value
     */
    fun addHeader(key: String?, value: String?)

    @Throws(IOException::class)
    fun write(bytes: ByteArray)

    @Throws(IOException::class)
    fun write(bytes: ByteArray, offset: Int, length: Int)
}