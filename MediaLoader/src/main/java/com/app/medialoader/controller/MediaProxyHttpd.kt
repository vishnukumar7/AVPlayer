package com.app.medialoader.controller

import com.app.medialoader.DownloadManager
import com.app.medialoader.MediaLoaderConfig
import com.app.medialoader.download.DownloadListener
import com.app.medialoader.tinyhttpd.TinyHttpd
import com.app.medialoader.tinyhttpd.request.Request
import com.app.medialoader.tinyhttpd.response.Response
import com.app.medialoader.tinyhttpd.response.ResponseException
import com.app.medialoader.utils.LogUtil
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * http服务器，将请求分发到[MediaController]进行处理
 *
 * @author vincanyang
 */
class MediaProxyHttpd @JvmOverloads constructor(videoLoaderConfig: MediaLoaderConfig? = null) :
    TinyHttpd() {
    private val mLock = Any()

    /**
     * 相当于web.xml，用于存储url和handler之间的映射
     */
    private val mReuqestHandlerMap: MutableMap<String?, MediaController> = ConcurrentHashMap()
    private var mMediaLoaderConfig: MediaLoaderConfig?



    override fun doGet(request: Request?, response: Response) {
        if (request != null) {
            if (mMediaLoaderConfig?.let { DownloadManager.getInstance(it.context) }!!
                    .isRunning(request.url())
            ) {
                DownloadManager.getInstance(mMediaLoaderConfig!!.context)!!.stop(request.url())
                LogUtil.d("Url " + request.url() + " is preDownloading,now be canceled by proxy download")
            }
            getMediaController(request.url()).responseByRequest(request, response)
        }

    }

    fun setVideoLoaderConfig(videoLoaderConfig: MediaLoaderConfig) {
        mMediaLoaderConfig = videoLoaderConfig
    }

    private fun getMediaController(url: String?): MediaController {
        synchronized(mLock) {

            var client = mReuqestHandlerMap[url]
            if (client == null) {
                client = MediaController(url, mMediaLoaderConfig)
                mReuqestHandlerMap[url] = client
            }
            return client
        }
    }

    private fun destroyMediaControllers() {
        synchronized(mLock) {
            for (clients in mReuqestHandlerMap.values) {
                clients.destroy()
            }
            mReuqestHandlerMap.clear()
        }
    }

    fun pauseDownload(url: String?) {
        getMediaController(url).pauseDownload(url)
    }

    fun resumeDownload(url: String?) {
        getMediaController(url).resumeDownload(url)
    }

    fun addDownloadListener(url: String?, listener: DownloadListener?) {
        getMediaController(url).addDownloadListener(listener)
    }

    fun removeDownloadListener(url: String?, listener: DownloadListener?) {
        getMediaController(url).removeDownloadListener(listener)
    }

    fun removeDownloadListener(listener: DownloadListener?) {
        for (requestHandler in mReuqestHandlerMap.values) {
            requestHandler.removeDownloadListener(listener)
        }
    }

    override fun shutdown() {
        super.shutdown()
        destroyMediaControllers()
    }

    init {
        mMediaLoaderConfig = videoLoaderConfig
    }
}