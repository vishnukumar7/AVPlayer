package com.app.medialoader.tinyhttpd.codec

import com.app.medialoader.tinyhttpd.response.Response
import java.io.IOException

/**
 * [Response]的编码器
 *
 * @author vincanyang
 */
interface ResponseEncoder<T : Any> {
    @Throws(IOException::class)
    fun encode(response: Any): ByteArray
}