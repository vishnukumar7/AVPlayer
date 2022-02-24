package com.app.medialoader.controller

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.app.medialoader.MediaLoaderConfig
import com.app.medialoader.data.DefaultDataSourceFactory
import com.app.medialoader.data.url.UrlDataSource
import com.app.medialoader.download.DownloadListener
import com.app.medialoader.manager.MediaManager
import com.app.medialoader.manager.MediaManagerImpl
import com.app.medialoader.tinyhttpd.request.Request
import com.app.medialoader.tinyhttpd.response.Response
import com.app.medialoader.tinyhttpd.response.ResponseException
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Media请求控制器
 *
 * @author vincanyang
 */
class MediaController(private val mUrl: String?, mediaLoaderConfig: MediaLoaderConfig?) {
    private val mCacheListeners= Collections.synchronizedList(
        LinkedList<DownloadListener>()
    )
    private val mMediaLoaderConfig: MediaLoaderConfig? = mediaLoaderConfig
    private var mMediaManager: MediaManager? = null
    @Throws(ResponseException::class, IOException::class)
    fun responseByRequest(request: Request?, response: Response?) {
        if (mMediaManager == null) {
            val urlDataSource: UrlDataSource = DefaultDataSourceFactory.createUrlDataSource(mUrl)

            mMediaLoaderConfig?.let {
                val file=File(it.cacheRootDir,it.cacheFileNameGenerator?.create(mUrl!!))
                val fileDataSource=DefaultDataSourceFactory.createFileDataSource(file,it.diskLruCache)
                mMediaManager = it.downloadExecutorService?.let { it1 ->
                    MediaManagerImpl(
                        urlDataSource,
                        fileDataSource,
                        MainThreadCacheListener(mUrl, mCacheListeners),
                        it1
                    )
                }
            }
        }
        try {
            mMediaManager?.responseByRequest(request!!, response!!)
        } finally {
            mMediaManager?.destroy()
            mMediaManager = null
        }
    }

    fun pauseDownload(url: String?) {
        mMediaManager?.pauseDownload(url)

    }

    fun resumeDownload(url: String?) {
        mMediaManager?.resumeDownload(url)
    }

    fun addDownloadListener(listener: DownloadListener?) {
        mCacheListeners.add(listener)
    }

    fun removeDownloadListener(listener: DownloadListener?) {
        mCacheListeners.remove(listener)
    }

    fun destroy() {
        mCacheListeners.clear()
        mMediaManager?.destroy()
        mMediaManager = null

    }

    private class MainThreadCacheListener(
        private val url: String?,
        listeners: List<DownloadListener?>
    ) : Handler(Looper.getMainLooper()), DownloadListener {
        private val listeners: List<DownloadListener?> = listeners
        override fun onProgress(url: String?, file: File?, progress: Int) {
            val message = obtainMessage()
            message.arg1 = progress
            message.obj = file
            sendMessage(message)
        }

        override fun onError(e: Throwable?) {}
        override fun handleMessage(msg: Message) {
            for (cacheListener in listeners) {
                cacheListener?.onProgress(url, msg.obj as File, msg.arg1)
            }
        }

    }

}