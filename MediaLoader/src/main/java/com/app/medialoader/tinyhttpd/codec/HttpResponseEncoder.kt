package com.app.medialoader.tinyhttpd.codec

import com.app.medialoader.tinyhttpd.HttpConstants
import com.app.medialoader.tinyhttpd.response.HttpResponse
import com.app.medialoader.tinyhttpd.response.Response
import java.io.IOException

/**
 * [HttpResponse]的编码器
 *
 * @author vincanyang
 */
class HttpResponseEncoder : ResponseEncoder<HttpResponse> {
    @Throws(IOException::class)
    override fun encode(res: Any): ByteArray {
        val sb = StringBuilder()
        val response: Response=res as Response
        sb.append(response.protocol().toString()).append(HttpConstants.SP)
            .append(response.status().toString())
        sb.append(HttpConstants.CRLF) //status line end
        for ((key, value) in response.headers()) {
            sb.append(key).append(HttpConstants.COLON).append(value).append(HttpConstants.CRLF)
        }
        sb.append(HttpConstants.CRLF) //headers end
        return sb.toString().toByteArray()
    }
}