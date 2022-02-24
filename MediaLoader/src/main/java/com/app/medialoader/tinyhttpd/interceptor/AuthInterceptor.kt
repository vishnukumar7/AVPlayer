package com.app.medialoader.tinyhttpd.interceptor

import android.text.TextUtils
import com.app.medialoader.tinyhttpd.HttpConstants
import com.app.medialoader.tinyhttpd.response.HttpStatus
import com.app.medialoader.tinyhttpd.response.ResponseException
import com.app.medialoader.utils.Util.getHmacSha1
import java.io.IOException

/**
 * 身份认证拦截器
 *
 * @author wencanyang
 */
class AuthInterceptor(private val mSecret: String) : Interceptor {
    @Throws(ResponseException::class, IOException::class)
    override fun intercept(chain: Interceptor.Chain) {
        val request = chain.request()
        val clientSign = request!!.getParam(HttpConstants.PARAM_SIGN)
        if (TextUtils.isEmpty(clientSign)) {
            throw ResponseException(
                HttpStatus.BAD_REQUEST,
                HttpConstants.PARAM_SIGN + " cann't be empty"
            )
        }
        val clientTimestamp = request.getParam(HttpConstants.PARAM_TIMESTAMP)
        if (TextUtils.isEmpty(clientTimestamp)) {
            throw ResponseException(
                HttpStatus.BAD_REQUEST,
                HttpConstants.PARAM_TIMESTAMP + " cann't be empty"
            )
        }
        val serverSign = getHmacSha1(request.url() + clientTimestamp, mSecret)
        if (serverSign != clientSign) {
            throw ResponseException(
                HttpStatus.UNAUTHORIZED,
                HttpConstants.PARAM_SIGN + " is not correct"
            )
        }
        chain.proceed(request, chain.response())
    }
}