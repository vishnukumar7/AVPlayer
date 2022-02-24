package com.app.medialoader.tinyhttpd.interceptor

import com.app.medialoader.tinyhttpd.request.Request
import com.app.medialoader.tinyhttpd.response.Response
import com.app.medialoader.tinyhttpd.response.ResponseException
import java.io.IOException

/**
 * 拦截器链实现
 *
 * @author vincanyang
 */
class InterceptorChainImpl(
    private val mInterceptors: List<Interceptor>,
    private val mIndex: Int,
    private val mRequest: Request?,
    private val mResponse: Response?
) : Interceptor.Chain {
    override fun request(): Request? {
        return mRequest
    }

    override fun response(): Response? {
        return mResponse
    }

    @Throws(ResponseException::class, IOException::class)
    override fun proceed(request: Request?, response: Response?) {
        if (mIndex >= mInterceptors.size) {
            throw AssertionError()
        }
        val next = InterceptorChainImpl(
            mInterceptors, mIndex + 1, request, response
        )
        val interceptor = mInterceptors[mIndex]
        interceptor.intercept(next)
    }
}