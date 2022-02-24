package com.app.medialoader.tinyhttpd.response

import com.app.medialoader.tinyhttpd.HttpHeaders
import com.app.medialoader.tinyhttpd.HttpVersion
import com.app.medialoader.utils.Util
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

/**
 * http响应
 *
 * @author vincanyang
 */
class HttpResponse(private val mChannel: SocketChannel) : Response {
    private val mHeaders = HttpHeaders()
    private val mHttpVersion = HttpVersion.HTTP_1_1
    private var mStatus = HttpStatus.OK
    private val mResponseByteBuffer = ByteBuffer.allocate(Util.DEFAULT_BUFFER_SIZE)
    override fun setStatus(status: HttpStatus) {
        mStatus = status
    }

    override fun addHeader(key: String?, value: String?) {
        mHeaders[key!!] = value!!
    }

    @Throws(IOException::class)
    override fun write(bytes: ByteArray) {
        write(bytes, 0, bytes.size)
    }

    @Throws(IOException::class)
    override fun write(bytes: ByteArray, offset: Int, length: Int) {
        mResponseByteBuffer.put(bytes, offset, length)
        mResponseByteBuffer.flip()
        while (mResponseByteBuffer.hasRemaining()) { //XXX 巨坑：ByteBuffer会缓存，可能不会全部写入channel
            mChannel.write(mResponseByteBuffer)
        }
        mResponseByteBuffer.clear()
    }

    override fun status(): HttpStatus {
        return mStatus
    }

    override fun protocol(): HttpVersion {
        return mHttpVersion
    }

    override fun headers(): HttpHeaders {
        return mHeaders
    }

    override fun toString(): String {
        return "HttpResponse{" +
                "httpVersion=" + mHttpVersion +
                ", status=" + mStatus +
                '}'
    }
}