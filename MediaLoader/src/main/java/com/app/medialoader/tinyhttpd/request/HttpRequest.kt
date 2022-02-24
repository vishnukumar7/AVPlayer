package com.app.medialoader.tinyhttpd.request

import com.app.medialoader.tinyhttpd.HttpHeaders
import com.app.medialoader.tinyhttpd.HttpVersion
import com.app.medialoader.utils.Util.notEmpty

class HttpRequest internal constructor(builder: Builder) : Request {
    private val mMethod: HttpMethod = builder.method
    private val mUrl: String? = builder.url
    private val mVersion: HttpVersion = builder.version
    private val mHeaders: HttpHeaders = builder.headers
    private val mParams: MutableMap<String, MutableList<String?>> = builder.params
    override fun method(): HttpMethod {
        return mMethod
    }

    override fun url(): String? {
        return mUrl
    }

    override fun protocol(): HttpVersion {
        return mVersion
    }

    override fun headers(): HttpHeaders {
        return mHeaders
    }

    override fun getParam(name: String): String {
        val param = mParams[name]
        return param?.get(0) ?: ""
    }

    override fun toString(): String {
        return "HttpRequest{" +
                "method=" + mMethod +
                ", url=" + mUrl +
                ", protocol='" + mVersion + '\'' +
                '}'
    }

    class Builder {
        var method: HttpMethod
        var url: String? = null
        var version: HttpVersion
        var headers: HttpHeaders
        var params: MutableMap<String, MutableList<String?>>
        fun method(method: HttpMethod?): Builder {
            this.method = notEmpty(method)
            return this
        }

        fun url(url: String?): Builder {
            this.url = notEmpty(url)
            return this
        }

        fun version(version: HttpVersion?): Builder {
            this.version = notEmpty(version)
            return this
        }

        fun headers(headers: HttpHeaders?): Builder {
            this.headers = notEmpty(headers)
            return this
        }

        fun params(params: MutableMap<String, MutableList<String?>>): Builder {
            this.params = notEmpty(params)
            return this
        }

        fun build(): HttpRequest {
            return HttpRequest(this)
        }

        init {
            method = HttpMethod.GET
            version = HttpVersion.HTTP_1_1
            headers = HttpHeaders()
            params = HashMap()
        }
    }

}