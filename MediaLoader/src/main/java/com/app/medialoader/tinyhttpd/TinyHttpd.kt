package com.app.medialoader.tinyhttpd

import com.app.medialoader.tinyhttpd.codec.HttpResponseEncoder
import com.app.medialoader.tinyhttpd.interceptor.AuthInterceptor
import com.app.medialoader.tinyhttpd.interceptor.Interceptor
import com.app.medialoader.tinyhttpd.interceptor.InterceptorChainImpl
import com.app.medialoader.tinyhttpd.interceptor.LoggingInterceptor
import com.app.medialoader.tinyhttpd.request.HttpMethod
import com.app.medialoader.tinyhttpd.request.Request
import com.app.medialoader.tinyhttpd.response.HttpStatus
import com.app.medialoader.tinyhttpd.response.Response
import com.app.medialoader.tinyhttpd.response.ResponseException
import com.app.medialoader.utils.LogUtil
import com.app.medialoader.utils.LogUtil.i
import com.app.medialoader.utils.Util
import com.app.medialoader.utils.Util.createUrl
import java.io.IOException
import java.net.InetAddress
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.collections.ArrayList


abstract class TinyHttpd{

    companion object{
        val TAG = TinyHttpd::class.java.simpleName
    }

    private var mHost: String? = null

    private var mReactorThread: Thread? = null

    private var mDispatchRunnable: DispatchHandler? = null

    private var mEchoTester: EchoTester? = null

    private val mInterceptors= Collections.synchronizedList(LinkedList<Interceptor>())

    private var mRandomUUID: String? = null

    private val mResponseEncoder= HttpResponseEncoder()

    @Throws(InterruptedException::class,IOException::class)
    constructor() : this(Util.LOCALHOST, 0)


    /*@Throws(InterruptedException::class, IOException::class)
    constructor(host: String) : this() {
        this(host, 0) //端口将会随机
    }*/
    constructor(host: String){

    }

    private inner class LastInterceptor : Interceptor {
        @Throws(ResponseException::class, IOException::class)
        override fun intercept(chain: Interceptor.Chain) {
            val request: Request? = chain.request()
            val response: Response? = chain.response()
            request?.let {
                when (it.method()) {
                    HttpMethod.GET -> {
                        response?.let { it1 -> doGet(it, it1) }
                    }
                    HttpMethod.POST -> {
                        response?.let { it1 -> doPost(it, it1) }
                    }
                }
            }

        }
    }





    @Throws(InterruptedException::class, IOException::class)
    constructor(host: String,port: Int)  {
        mHost = host
        val threadStartSignal = CountDownLatch(1)
        mDispatchRunnable =
            DispatchHandler(InetAddress.getByName(host), port, threadStartSignal, this)
        mReactorThread = Thread(mDispatchRunnable)
       mReactorThread?.let {
           it.isDaemon=false
           it.name="TinyHttp thread"
           it.start()
           threadStartSignal.await()
           mEchoTester = EchoTester(host, getPort())
           mRandomUUID = UUID.randomUUID().toString()
           LogUtil.d("TinyHttp is working?" + isWorking())
       }

    }

    open fun createUrl(pathOfUrl: String?): String? {
        return createUrl(
            mHost, getPort(), pathOfUrl!!,
            mRandomUUID!!
        )
    }

    @Throws(ResponseException::class, IOException::class)
    protected open fun doGet(request: Request?, response: Response) {
        response.setStatus(HttpStatus.NOT_FOUND)
        val headersBytes = mResponseEncoder.encode(response)
        response.write(headersBytes)
    }

    @Throws(ResponseException::class, IOException::class)
    protected open fun doPost(request: Request?, response: Response) {
        response.setStatus(HttpStatus.NOT_FOUND)
        val headersBytes = mResponseEncoder.encode(response)
        response.write(headersBytes)
    }

    open fun isWorking(): Boolean {
        return mEchoTester!!.request()
    }
    open fun getPort(): Int {
        return mDispatchRunnable!!.getPort()
    }

    open fun addInterceptor(interceptor: Interceptor?) {
        mInterceptors.add(interceptor)
    }

    open fun shutdown() {
        i(TAG, "Destroy TinyHttp")
        if (mDispatchRunnable != null) {
            mDispatchRunnable!!.destroy()
        }
    }

    @Throws(ResponseException::class, IOException::class)
    open fun service(request: Request?, response: Response?) {
        if (mEchoTester!!.isEchoRequest(request!!)) {
            mEchoTester!!.response(response!!)
        } else {
            response?.let { serviceWithInterceptorChain(request, it) }
        }
    }

    @Throws(ResponseException::class, IOException::class)
    private fun serviceWithInterceptorChain(
        originalRequest: Request,
        originalResponse: Response
    ) {
        val interceptors: MutableList<Interceptor> = ArrayList()
        interceptors.add(AuthInterceptor(mRandomUUID!!))
        interceptors.add(LoggingInterceptor())
        interceptors.addAll(mInterceptors)
        interceptors.add(LastInterceptor()) //必须放最后一个才能结束InterceptorChain
        val chain: Interceptor.Chain =
            InterceptorChainImpl(interceptors, 0, originalRequest, originalResponse)
        chain.proceed(originalRequest, originalResponse)
    }
}