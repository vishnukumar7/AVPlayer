package com.app.medialoader.manager

import android.text.TextUtils
import com.app.medialoader.data.DefaultDataSourceFactory
import com.app.medialoader.data.file.FileDataSource
import com.app.medialoader.data.url.DefaultUrlDataSource
import com.app.medialoader.data.url.UrlDataSource
import com.app.medialoader.download.DownloadListener
import com.app.medialoader.download.DownloadTask
import com.app.medialoader.tinyhttpd.HttpHeaders
import com.app.medialoader.tinyhttpd.codec.HttpResponseEncoder
import com.app.medialoader.tinyhttpd.request.Request
import com.app.medialoader.tinyhttpd.response.HttpResponse
import com.app.medialoader.tinyhttpd.response.HttpStatus
import com.app.medialoader.tinyhttpd.response.Response
import com.app.medialoader.tinyhttpd.response.ResponseException
import com.app.medialoader.utils.Util
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * Media业务实现
 *
 * @author vincanyang
 */
class MediaManagerImpl(
    urlDataSource: UrlDataSource,
    fileDataSource: FileDataSource,
    downloadListener: DownloadListener,
    downloadExecutorService: ExecutorService
) : MediaManager {
    private val mUrlDataSource: UrlDataSource
    private val mFileDataSource: FileDataSource
    private val mDownloadListener: DownloadListener
    private val mWaitForDownloadLock = ReentrantLock().newCondition()

    @Volatile
    private var mDownloadThread: Thread? = null
    private val mDownloadTask: DownloadTask?
    private val mDownloadExecutorService: ExecutorService
    private val mResponseEncoder= HttpResponseEncoder()
    @Throws(ResponseException::class, IOException::class)
    override fun responseByRequest(request: Request, response: Response) {
        addResponseHeaders(request, response)
        val headersBytes: ByteArray = mResponseEncoder.encode(response as HttpResponse)
        response.write(headersBytes)
        val rangeOffset = request.headers().getRangeOffset()
        if (isCacheDataEnough(request)) {
            responseWithCache(rangeOffset, response)
        } else {
            responseWithUrl(rangeOffset, response)
        }
    }

    @Throws(IOException::class)
    private fun addResponseHeaders(request: Request, response: Response) {
        response.setStatus(
            if (request.headers().isPartial()) HttpStatus.PARTIAL_CONTENT else HttpStatus.OK
        )
        response.addHeader(HttpHeaders.Names.ACCEPT_RANGES, HttpHeaders.Values.BYTES)
        val length =
            if (mFileDataSource.isCompleted) mFileDataSource.length() else mUrlDataSource.length()
        if (length >= 0) {
            val contentLength = if (request.headers().isPartial()) length - request.headers()
                .getRangeOffset() else length
            response.addHeader(HttpHeaders.Names.CONTENT_LENGTH, contentLength.toString())
        }
        if (length >= 0 && request.headers().isPartial()) {
            response.addHeader(
                HttpHeaders.Names.CONTENT_RANGE,
                String.format(
                    HttpHeaders.Values.BYTES + " %d-%d/%d",
                    request.headers().getRangeOffset(),
                    length - 1,
                    length
                )
            )
        }
        val mimeType: String? = mUrlDataSource.mimeType()
        mimeType?.let {
            if (!TextUtils.isEmpty(it)) {
                response.addHeader(HttpHeaders.Names.CONTENT_TYPE, it)
            }
        }

    }

    @Throws(IOException::class)
    private fun isCacheDataEnough(request: Request): Boolean {
        val urlDataSourceLength: Long = mUrlDataSource.length()
        val sourceLengthKnown = urlDataSourceLength > 0
        val fileDataSourceLength = mFileDataSource.length()
        return !sourceLengthKnown || !request.headers().isPartial() || request.headers()
            .getRangeOffset() <= fileDataSourceLength + urlDataSourceLength * NO_CACHE_BARRIER
    }

    @Throws(ResponseException::class, IOException::class)
    private fun responseWithCache(rangeOffset: Long, response: Response) {
        var rangeOffset = rangeOffset
        val buffer = ByteArray(Util.DEFAULT_BUFFER_SIZE)
        var readBytes: Int
        while (seekAndRead(rangeOffset, buffer, buffer.size).also { readBytes = it } != -1) {
            response.write(buffer, 0, readBytes)
            rangeOffset += readBytes.toLong()
        }
    }

    @Throws(ResponseException::class, IOException::class)
    private fun seekAndRead(rangeOffset: Long, buffer: ByteArray, length: Int): Int {
        checkStartDownload() //启动下载
        while (!mFileDataSource.isCompleted && mFileDataSource.length() < rangeOffset + length) {
            waitForDownload()
        }
        return mFileDataSource.seekAndRead(rangeOffset, buffer)
    }

    @Synchronized
    @Throws(IOException::class)
    private fun checkStartDownload() {
        val isThreadStarted =
            mDownloadThread != null && mDownloadThread!!.state != Thread.State.TERMINATED
        if (!mFileDataSource.isCompleted && mDownloadTask?.isStopped() == true && !isThreadStarted) {
            mDownloadExecutorService.submit(mDownloadTask)
            mDownloadThread = mDownloadTask.currentThread
        }
    }

    @Throws(ResponseException::class)
    private fun waitForDownload() {
        synchronized(mWaitForDownloadLock) {
            try {
                mWaitForDownloadLock.await(1000,TimeUnit.MILLISECONDS)
            } catch (e: InterruptedException) {
                throw ResponseException(
                    HttpStatus.INTERNAL_ERROR,
                    "Waiting for downloading is interrupted"
                )
            }
        }
    }

    private inner class DownloadListenerImpl : DownloadListener {
        override fun onProgress(url: String?, file: File?, progress: Int) {
            synchronized(mWaitForDownloadLock) { mWaitForDownloadLock.signalAll() }
            mDownloadListener.onProgress(url, file, progress)
        }

        override fun onError(e: Throwable?) {
            mDownloadListener.onError(e)
        }
    }

    @Throws(IOException::class)
    private fun responseWithUrl(rangeOffset: Long, response: Response) {
        val newDataSource: UrlDataSource =
            DefaultDataSourceFactory.createUrlDataSource(mUrlDataSource as DefaultUrlDataSource)
        try {
            newDataSource.open(rangeOffset)
            val buffer = ByteArray(Util.DEFAULT_BUFFER_SIZE)
            var readBytes: Int
            while (newDataSource.read(buffer).also { readBytes = it } != -1) {
                response.write(buffer, 0, readBytes)
            }
        } finally {
            newDataSource.close()
        }
    }

    override fun pauseDownload(url: String?) {
        if (mDownloadTask != null) {
            mDownloadTask.pause()
        }
    }

    override fun resumeDownload(url: String?) {
        mDownloadTask?.resume()
    }

    override fun destroy() {
        mDownloadTask?.stop()
        if (mDownloadThread != null) {
            mDownloadThread!!.interrupt()
        }
    }

    companion object {
        private const val NO_CACHE_BARRIER = .2f
    }

    init {
        mUrlDataSource = urlDataSource
        mFileDataSource = fileDataSource
        mDownloadListener = downloadListener
        mDownloadTask = DownloadTask(mUrlDataSource, mFileDataSource, DownloadListenerImpl())
        mDownloadExecutorService = downloadExecutorService
    }
}