package com.app.medialoader.download

import com.app.medialoader.DownloadManager
import com.app.medialoader.MediaLoaderConfig
import com.app.medialoader.data.DefaultDataSourceFactory
import com.app.medialoader.data.DefaultDataSourceFactory.createFileDataSource
import com.app.medialoader.data.file.FileDataSource
import com.app.medialoader.data.url.UrlDataSource
import com.app.medialoader.utils.LogUtil.e
import com.app.medialoader.utils.Util
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

/**
 * 下载任务
 *
 * @author vincanyang
 */
class DownloadTask : Runnable {
    private var mUrlDataSource: UrlDataSource
    private var mFileDataSource: FileDataSource
    private val mStopLock = Any()

    @Volatile
    private var stopped = false
    private val mPauseLock = ReentrantLock().newCondition()
    private val mPaused = AtomicBoolean(false)

    @Volatile
    private var mDownloadPercent = 0
    private var mDownloadListener: DownloadListener?

    constructor(
        request: DownloadManager.Request,
        mediaLoaderConfig: MediaLoaderConfig?,
        listener: DownloadListener?
    ) {
        mUrlDataSource = DefaultDataSourceFactory.createUrlDataSource(request.url)
        mFileDataSource = createFileDataSource(
            File(
                mediaLoaderConfig?.cacheRootDir,
                mediaLoaderConfig?.cacheFileNameGenerator!!.create(request.url)
            ), mediaLoaderConfig?.diskLruCache
        )
        mDownloadListener = listener
    }

    constructor(
        urlDataSource: UrlDataSource,
        fileDataSource: FileDataSource,
        listener: DownloadListener?
    ) {
        mUrlDataSource = urlDataSource
        mFileDataSource = fileDataSource
        mDownloadListener = listener
    }

    override fun run() {
//        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);//避免和主线程抢占资源发生ANR
        val urlDataSourceLength: Long
        var offset: Long
        try {
            offset = mFileDataSource.length()
            mUrlDataSource.open(offset)
            urlDataSourceLength = mUrlDataSource.length()
            val buffer = ByteArray(Util.DEFAULT_BUFFER_SIZE)
            var readBytes: Int
            while (mUrlDataSource.read(buffer).also { readBytes = it } != -1) {
                waitIfPaused()
                synchronized(mStopLock) {
                    if (isStopped()) {
                        return
                    }
                    mFileDataSource.append(buffer, readBytes)
                }
                offset += readBytes.toLong()
                notifyNewDataAvailable(offset, urlDataSourceLength)
            }
            tryComplete()
        } catch (e: Throwable) {
            if (mDownloadListener != null) {
                mDownloadListener!!.onError(e)
            }
            e(e)
        } finally {
            try {
                mUrlDataSource.close()
            } catch (e: IOException) {
                e("error close url data source ", e)
            }
        }
    }

    private fun onDownloadPercentUpdated(downloadProgress: Int) {
        if (mDownloadListener != null) {
            mDownloadListener!!.onProgress(
                mUrlDataSource.url,
                mFileDataSource.file,
                downloadProgress
            )
            //            LogUtil.e("Url " + mUrlDataSource.getUrl() + " download progress:" + downloadProgress);
        }
    }

    private fun notifyNewDataAvailable(fileDataSourceAvailable: Long, urlDataSourceLength: Long) {
        val newPercent =
            if (urlDataSourceLength == 0L) 100 else (fileDataSourceAvailable * 100 / urlDataSourceLength).toInt()
        if (newPercent > mDownloadPercent + 2) { //防止通知太快导致ui掉帧
            onDownloadPercentUpdated(newPercent)
            mDownloadPercent = newPercent
        }
    }

    @Throws(IOException::class)
    private fun tryComplete() {
        mDownloadPercent = 100
        onDownloadPercentUpdated(mDownloadPercent)
        synchronized(mStopLock) {
            if (!isStopped() && mFileDataSource.length() == mUrlDataSource.length()) {
                mFileDataSource.complete()
            }
        }
    }

    fun isStopped(): Boolean {
        return Thread.currentThread().isInterrupted || stopped
    }

    private fun waitIfPaused() {
        if (mPaused.get()) {
            synchronized(mPauseLock) {
                try {
                    mPauseLock.await()
                } catch (e: InterruptedException) {
                }
            }
        }
    }

    fun pause() {
        mPaused.set(true)
    }

    fun resume() {
        mPaused.set(false)
        synchronized(mPauseLock) {
            mPauseLock.signal() }
    }

    val currentThread: Thread
        get() = Thread.currentThread()

    fun stop() {
        synchronized(mStopLock) {
            stopped = true
            try {
                mFileDataSource.close()
            } catch (e: IOException) {
                e("error close file dataSource", e)
            }
        }
    }
}