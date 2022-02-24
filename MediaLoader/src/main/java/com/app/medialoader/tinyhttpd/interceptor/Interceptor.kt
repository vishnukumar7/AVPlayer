package com.app.medialoader.tinyhttpd.interceptor

import com.app.medialoader.tinyhttpd.request.Request
import com.app.medialoader.tinyhttpd.response.Response
import com.app.medialoader.tinyhttpd.response.ResponseException
import java.io.IOException

/**
 * 拦截器
 *
 * @author wencanyang
 */
interface Interceptor {
    @Throws(ResponseException::class, IOException::class)
    fun intercept(chain: Chain)

    /**
     * 拦截器链
     */
    interface Chain {
        fun request(): Request?
        fun response(): Response?

        @Throws(ResponseException::class, IOException::class)
        fun proceed(request: Request?, response: Response?)
    }
}