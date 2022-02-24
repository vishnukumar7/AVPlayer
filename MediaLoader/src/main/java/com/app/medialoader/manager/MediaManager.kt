package com.app.medialoader.manager

import com.app.medialoader.tinyhttpd.request.Request
import com.app.medialoader.tinyhttpd.response.Response
import com.app.medialoader.tinyhttpd.response.ResponseException
import java.io.IOException

/**
 * Media业务接口
 *
 * @author vincanyang
 */
interface MediaManager {
    @Throws(ResponseException::class, IOException::class)
    fun responseByRequest(request: Request, response: Response)
    fun pauseDownload(url: String?)
    fun resumeDownload(url: String?)
    fun destroy()
}