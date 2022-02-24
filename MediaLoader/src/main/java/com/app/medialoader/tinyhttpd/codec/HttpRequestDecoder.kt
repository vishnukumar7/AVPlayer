package com.app.medialoader.tinyhttpd.codec

import android.text.TextUtils
import com.app.medialoader.tinyhttpd.HttpConstants
import com.app.medialoader.tinyhttpd.HttpHeaders
import com.app.medialoader.tinyhttpd.HttpVersion
import com.app.medialoader.tinyhttpd.request.HttpMethod
import com.app.medialoader.tinyhttpd.request.HttpRequest
import com.app.medialoader.tinyhttpd.response.HttpStatus
import com.app.medialoader.tinyhttpd.response.ResponseException
import com.app.medialoader.utils.Util.decode
import java.util.*

class HttpRequestDecoder : RequestDecoder<HttpRequest?> {
    @Throws(ResponseException::class)
    override fun decode(bytes: ByteArray?): HttpRequest {
        val rawRequest = String(bytes!!)
        return try {
            //Http协议第1行是Method URI VERSION
            val st = StringTokenizer(rawRequest)
            if (!st.hasMoreTokens()) {
                throw ResponseException(
                    HttpStatus.BAD_REQUEST,
                    "BAD REQUEST: Syntax error. Usage: GET /example/originFile.html"
                )
            }
            val method = HttpMethod[st.nextToken().uppercase()]
            if (!st.hasMoreTokens()) {
                throw ResponseException(
                    HttpStatus.BAD_REQUEST,
                    "BAD REQUEST: Missing URI. Usage: GET /example/originFile.html"
                )
            }
            var url: String? = st.nextToken().substring(1)
            // decode params
            val questionMarkIndex = url!!.indexOf('?')
            val params: MutableMap<String, MutableList<String?>> = HashMap()
            url = if (questionMarkIndex >= 0) {
                decodeParms(url.substring(questionMarkIndex + 1), params)
                decode(url.substring(0, questionMarkIndex))
            } else {
                decode(url)
            }
            val httpVersion = HttpVersion[st.nextToken()]
            //第2行起都是KEY:VALUE格式的header
            val headers = HttpHeaders()
            val lines = rawRequest.split(HttpConstants.CRLF).toTypedArray()
            for (i in 1 until lines.size) { //igore the first line
                val keyVal = lines[i].split(HttpConstants.COLON, limit = 2).toTypedArray()
                if (!TextUtils.isEmpty(keyVal[0]) && !TextUtils.isEmpty(keyVal[1])) {
                    headers[keyVal[0]] = keyVal[1]
                }
            }
            HttpRequest.Builder().method(method).url(url).version(httpVersion).headers(headers).params(params)
                .build()
        } catch (ex: Exception) {
            throw ResponseException(
                HttpStatus.INTERNAL_ERROR,
                "SERVER INTERNAL ERROR: IOException: " + ex.message,
                ex
            )
        }
    }

    private fun decodeParms(queryString: String, params: MutableMap<String, MutableList<String?>>) {
        val st = StringTokenizer(queryString, "&")
        while (st.hasMoreTokens()) {
            val paramStr = st.nextToken()
            val equalMarkIndex = paramStr.indexOf('=')
            var key: String
            var value: String?
            if (equalMarkIndex >= 0) {
                key = decode(paramStr.substring(0, equalMarkIndex))!!
                    .trim { it <= ' ' }
                value = decode(paramStr.substring(equalMarkIndex + 1))
            } else {
                key = decode(paramStr)!!.trim { it <= ' ' }
                value = ""
            }
            var values = params[key]
            if (values == null) {
                values = ArrayList()
                params[key] = values
            }
            values.add(value)
        }
    }
}