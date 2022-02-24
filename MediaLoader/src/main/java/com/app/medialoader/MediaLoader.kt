package com.app.medialoader

import android.content.Context
import android.net.Uri
import com.app.medialoader.controller.MediaProxyHttpd
import com.app.medialoader.utils.Util.notEmpty
import java.io.File
import java.io.IOException



class MediaLoader private constructor(context: Context) {
    var mMediaLoaderConfig: MediaLoaderConfig? = null
    private var mMediaHttpd: MediaProxyHttpd? = null

    /**
     * 初始化设置
     *
     * @param mediaLoaderConfig 设置选项
     */
    fun init(mediaLoaderConfig: MediaLoaderConfig?) {
        mMediaLoaderConfig = notEmpty(mediaLoaderConfig)
        mMediaHttpd!!.setVideoLoaderConfig(mediaLoaderConfig!!)
    }

    /**
     * 获取代理url
     *
     * @param url 原url
     * @return
     */
    fun getProxyUrl(url: String): String? {
        return getProxyUrl(url, true)
    }

    /**
     * 获取代理url
     *
     * @param url
     * @return
     */
    private fun getProxyUrl(url: String, isAllowUriFromFile: Boolean): String? {
        if (isAllowUriFromFile) {
            val file = getCacheFile(url)
            if (file!!.exists()) {
                return Uri.fromFile(file).toString() //originFile://url
            }
        }
        return if (mMediaHttpd!!.isWorking()) {
            mMediaHttpd!!.createUrl(url) //http://127.0.0.1:8090/path
        } else {
            url
        }
    }

    /**
     * 是否缓存文件
     *
     * @param url 文件url
     * @return
     */
    fun isCached(url: String?): Boolean {
        return getCacheFile(url)!!.exists()
    }

    /**
     * 获取缓存的文件
     *
     * @param url 文件url
     * @return
     */
    fun getCacheFile(url: String?): File? {
        return mMediaLoaderConfig!!.diskLruCache[notEmpty(url)]
    }

    /**
     * 添加下载监听器
     *
     * @param url      文件url
     * @param listener 下载监听器
     */
    fun addDownloadListener(
        url: String?,
        listener: com.app.medialoader.download.DownloadListener?
    ) {
        mMediaHttpd!!.addDownloadListener(notEmpty(url), notEmpty(listener))
    }

    /**
     * 删除下载监听器
     *
     * @param url      文件url
     * @param listener 下载监听器
     */
    fun removeDownloadListener(
        url: String?,
        listener: com.app.medialoader.download.DownloadListener?
    ) {
        mMediaHttpd!!.removeDownloadListener(notEmpty(url), notEmpty(listener))
    }

    /**
     * 删除下载监听器（当你无法得知文件url时使用）
     *
     * @param listener 下载监听器
     */
    fun removeDownloadListener(listener: com.app.medialoader.download.DownloadListener?) {
        mMediaHttpd!!.removeDownloadListener(notEmpty(listener))
    }

    /**
     * 暂停下载
     *
     * @param url 文件url
     */
    fun pauseDownload(url: String?) {
        mMediaHttpd!!.pauseDownload(notEmpty(url))
    }

    /**
     * 继续下载
     *
     * @param url 文件url
     */
    fun resumeDownload(url: String?) {
        mMediaHttpd!!.resumeDownload(notEmpty(url))
    }

    /**
     * 销毁实例
     */
    fun destroy() {
        if (mMediaHttpd != null) {
            mMediaHttpd!!.shutdown()
            mMediaHttpd = null
        }
        if (mMediaLoaderConfig != null) {
            mMediaLoaderConfig!!.downloadExecutorService!!.shutdownNow()
            mMediaLoaderConfig = null
        }
    }

    companion object {
        val TAG = MediaLoader::class.java.simpleName

        @Volatile
        private var sInstance: MediaLoader? = null

        /**
         * 创建实例
         *
         * @param context
         * @return
         */
        fun getInstance(context: Context?): MediaLoader? {
            if (sInstance == null) {
                synchronized(MediaLoader::class.java) {
                    if (sInstance == null) {
                        sInstance = MediaLoader(
                            notEmpty(context)
                                .applicationContext
                        )
                    }
                }
            }
            return sInstance
        }
    }

    init {
        try {
            mMediaHttpd = MediaProxyHttpd()
            init(MediaLoaderConfig.Builder(context).build()) //使用默认的VideoLoaderConfig
        } catch (e: InterruptedException) {
            destroy()
            throw IllegalStateException("error init medialoader", e)
        } catch (e: IOException) {
            destroy()
            throw IllegalStateException("error init medialoader", e)
        }
    }
}