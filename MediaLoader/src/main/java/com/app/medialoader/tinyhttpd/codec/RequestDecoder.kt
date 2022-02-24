package com.app.medialoader.tinyhttpd.codec

import com.app.medialoader.tinyhttpd.request.Request
import com.app.medialoader.tinyhttpd.response.ResponseException

interface RequestDecoder<T : Request?> {
    @Throws(ResponseException::class)
    fun decode(bytes: ByteArray?): T
}