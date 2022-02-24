package com.app.medialoader.tinyhttpd

import com.app.medialoader.data.DefaultDataSourceFactory.createUrlDataSource
import com.app.medialoader.tinyhttpd.codec.HttpResponseEncoder
import com.app.medialoader.tinyhttpd.codec.ResponseEncoder
import com.app.medialoader.tinyhttpd.request.Request
import com.app.medialoader.tinyhttpd.response.Response
import com.app.medialoader.utils.LogUtil.e
import com.app.medialoader.utils.LogUtil.i
import com.app.medialoader.utils.Util.createUrl
import java.io.IOException
import java.util.*
import java.util.concurrent.*

/**
 * httpd的echo测试,检测httpd是否运行
 *
 * @author vincanyang
 */
class EchoTester(private val mHost: String, private val mPort: Int) {
    private val mExecutorService = Executors.newSingleThreadExecutor() //FIXME
    private val mResponseEncoder: ResponseEncoder<*> = HttpResponseEncoder()
    fun isEchoRequest(request: Request): Boolean {
        return URL == request.url()
    }

    fun request(): Boolean {
        val echoFutureTask = mExecutorService.submit(RequestEchoCallable())
        try {
            return echoFutureTask[TIMEOUT_AWAIT, TimeUnit.MILLISECONDS]
        } catch (e: TimeoutException) {
            e("Echo httpd timeout $TIMEOUT_AWAIT", e)
        } catch (e: InterruptedException) {
            e("Error echo httpd", e)
        } catch (e: ExecutionException) {
            e("Error echo httpd", e)
        }
        return false
    }

    @Throws(IOException::class)
    private fun requestEcho(): Boolean {
        val source = createUrlDataSource(createUrl(mHost, mPort, URL))
        return try {
            source.open(0)
            val expectedResponse = RESPONSE.toByteArray()
            val actualResponse = ByteArray(expectedResponse.size)
            source.read(actualResponse)
            val isOk = Arrays.equals(expectedResponse, actualResponse)
            i("Echo is ok?$isOk")
            isOk
        } catch (e: IOException) {
            e("Error reading echo response", e)
            false
        } finally {
            source.close()
        }
    }

    private inner class RequestEchoCallable : Callable<Boolean> {
        @Throws(Exception::class)
        override fun call(): Boolean {
            return requestEcho()
        }
    }

    @Throws(IOException::class)
    fun response(response: Response) {
        val headersBytes = mResponseEncoder.encode(response)
        response.write(headersBytes)
        val bodyBytes = RESPONSE.toByteArray()
        response.write(bodyBytes)
    }

    companion object {
        private const val URL = "echo"
        private const val RESPONSE = "echo ok"
        private const val TIMEOUT_AWAIT: Long = 300 //300ms
    }
}