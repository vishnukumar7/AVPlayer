package com.app.medialoader

import android.content.Context
import android.util.Pair
import com.app.medialoader.download.DownloadListener
import com.app.medialoader.download.DownloadTask
import com.app.medialoader.utils.FileUtil
import com.app.medialoader.utils.Util
import com.app.medialoader.utils.Util.notEmpty
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService


class DownloadManager {

    private var mDownloadExecutorService: ExecutorService? = null

    private var mDownloaderTaskMap=ConcurrentHashMap<String, DownloadTask>()

    private var mMediaLoaderConfig: MediaLoaderConfig?


    companion object{

        @Volatile
        private var sInstance: DownloadManager? = null

        fun getInstance(context: Context): DownloadManager? {
            if (sInstance == null) {
                synchronized(DownloadManager::class.java) {
                    if (sInstance == null) {
                        sInstance = DownloadManager(context.applicationContext)
                    }
                }
            }
            return sInstance
        }

    }

    constructor(context: Context) {
        mDownloaderTaskMap = ConcurrentHashMap()
        mMediaLoaderConfig = MediaLoader.getInstance(context)!!.mMediaLoaderConfig
    }

    fun enqueue(request: Request) {
        enqueue(request, null)
    }

    fun enqueue(request: Request, listener: DownloadListener?) {
        notEmpty(request)
        if (!request.forceDownload) {
            if (isCached(request.url)) {
                return
            }
        }
        val task = DownloadTask(request, mMediaLoaderConfig, listener)
        mDownloaderTaskMap.put(request.url, task)
        if (mDownloadExecutorService == null) {
            mDownloadExecutorService = DefaultConfigFactory.createPredownloadExecutorService()
        }
        mDownloadExecutorService?.submit(task)
    }

    fun isRunning(url: String?): Boolean {
        val task: DownloadTask? = mDownloaderTaskMap[notEmpty(url)]
        return if (task != null) {
            !task.isStopped()
        } else false
    }

    fun pause(url: String?) {
        mDownloaderTaskMap[notEmpty(url)]?.pause()
    }


    fun resume(url: String?) {
        mDownloaderTaskMap[notEmpty(url)]?.resume()
    }


    fun stop(url: String?) {
        val task: DownloadTask? = mDownloaderTaskMap.remove(notEmpty(url))
        task?.stop()
    }


    fun pauseAll() {
        for (task in mDownloaderTaskMap.values) {
            task.pause()
        }
    }


    fun resumeAll() {
        for (task in mDownloaderTaskMap.values) {
            task.resume()
        }
    }

    fun stopAll() {
        for (task in mDownloaderTaskMap.values) {
            task.stop()
        }
        mDownloaderTaskMap.clear()
        if (mDownloadExecutorService != null) {
            mDownloadExecutorService!!.shutdownNow()
            mDownloadExecutorService = null
        }
    }


    fun isCached(url: String?): Boolean {
        return getCacheFile(url).exists()
    }


    fun getCacheFile(url: String?): File {
        return mMediaLoaderConfig!!.diskLruCache[notEmpty(url)]!!
    }

    @Throws(IOException::class)
    fun cleanCacheDir() {
        mMediaLoaderConfig!!.cacheRootDir?.let { FileUtil.cleanDir(it) }
    }

    class Request(url: String?) {
        val url: String = notEmpty(url)
        private val requestHeaders: MutableList<Pair<String, String>> = ArrayList()
        var forceDownload = false

        fun getRequestHeaders(): List<Pair<String, String>> {
            return requestHeaders
        }

        fun addRequestHeader(header: String?, value: String?): Request {
            var value = value
            if (header == null) {
                throw NullPointerException("header cannot be null")
            }
            require(!header.contains(":")) { "header may not contain ':'" }
            if (value == null) {
                value = ""
            }
            requestHeaders.add(Pair.create(header, value))
            return this
        }

        fun forceDownload(forceDownload: Boolean): Request {
            this.forceDownload = forceDownload
            return this
        }

    }
}