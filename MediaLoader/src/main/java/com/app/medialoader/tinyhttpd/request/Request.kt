package com.app.medialoader.tinyhttpd.request

import com.app.medialoader.tinyhttpd.HttpHeaders
import com.app.medialoader.tinyhttpd.HttpVersion

interface Request {
    fun method(): HttpMethod
    fun url(): String?
    fun protocol(): HttpVersion
    fun headers(): HttpHeaders
    fun getParam(name: String): String
}