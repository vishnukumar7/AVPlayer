package com.app.medialoader.tinyhttpd.interceptor

import com.app.medialoader.tinyhttpd.response.ResponseException
import com.app.medialoader.utils.LogUtil.e
import com.app.medialoader.utils.Util.decode
import java.io.IOException

/**
 * 日志拦截器
 *
 * @author wencanyang
 */
class LoggingInterceptor : Interceptor {
    @Throws(ResponseException::class, IOException::class)
    override fun intercept(chain: Interceptor.Chain) {
        val t1 = System.nanoTime()
        val request = chain.request()
        val response = chain.response()
        e(
            String.format(
                "Sending request %s with headers %n%s", decode(
                    request!!.url()
                ), request.headers()
            )
        )
        chain.proceed(request, response)
        val t2 = System.nanoTime()
        e(
            String.format(
                "Received response for %s in %.1fms with headers %n%s", decode(
                    request.url()
                ), (t2 - t1) / 1e6, response!!.headers()
            )
        )
    }
}