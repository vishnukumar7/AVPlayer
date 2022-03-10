package com.app.medialoader.tinyhttpd

import android.os.Process
import com.app.medialoader.tinyhttpd.codec.HttpRequestDecoder
import com.app.medialoader.tinyhttpd.codec.HttpResponseEncoder
import com.app.medialoader.tinyhttpd.codec.RequestDecoder
import com.app.medialoader.tinyhttpd.codec.ResponseEncoder
import com.app.medialoader.tinyhttpd.request.Request
import com.app.medialoader.tinyhttpd.response.HttpResponse
import com.app.medialoader.tinyhttpd.response.Response
import com.app.medialoader.tinyhttpd.response.ResponseException
import com.app.medialoader.utils.LogUtil.d
import com.app.medialoader.utils.LogUtil.e
import java.io.IOException
import java.nio.channels.ClosedChannelException
import java.nio.channels.SocketChannel

class IOHandler(channel: SocketChannel, requestByte: ByteArray, httpServer: TinyHttpd) : Runnable {


    private val mRequestDecoder= HttpRequestDecoder()

    private val mResponseEncoder = HttpResponseEncoder()

    private var mHttpServer: TinyHttpd = httpServer

    private var mChannel: SocketChannel = channel

    private val mReuqestBytes: ByteArray = requestByte

    override fun run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
        val response: Response = HttpResponse(mChannel)
        try {
            val request: Request = mRequestDecoder.decode(mReuqestBytes) //decode request
            mHttpServer.service(request, response) //handle request and response to client
        } catch (re: ResponseException) { //handle biz exception
            handleResponseException(re, response)
        } catch (e: IOException) {
            handleIOException(e)
        } finally {
            closeChannel()
        }
    }

    private fun handleResponseException(re: ResponseException, response: Response) {
        d("ResponseException happened and handling", re)
        response.setStatus(re.status)
        try {
            response.write(mResponseEncoder.encode(response))
            response.write(re.message?.toByteArray()!!)
        } catch (e: IOException) {
            e("Error writing the response$e")
        }
    }

    private fun handleIOException(e: IOException) {
        if (e is ClosedChannelException) {
            d("Client close the channel$e")
        } else {
            e("Error service$e")
        }
    }

    private fun closeChannel() {
        d("Closing the channel")
        try {
            mChannel.close()
        } catch (e: IOException) {
            e("Error closing the channel$e")
        }
    }
}