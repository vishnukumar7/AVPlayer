package com.app.medialoader

import android.content.Context
import com.app.medialoader.data.file.naming.FileNameCreator
import com.app.medialoader.data.file.naming.Md5FileNameCreator
import com.app.medialoader.utils.FileUtil.getDiskCacheDir
import java.io.File
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

object DefaultConfigFactory {
    private const val DIR_MEDIA_CACHE = "medialoader"
    const val DEFAULT_MAX_CACHE_FILES_SIZE = (500 * 1024 * 1024).toLong()
    const val DEFAULT_MAX_CACHE_FILES_COUNT = 500


    const val DEFAULT_MAX_CACHE_FILE_TIME_LIMIT = (10 * 24 * 60 * 60 //10Day
            ).toLong()

    const val DEFAULT_PROXY_DOWNLOAD_THREAD_POOL_SIZE = 3


    const val DEFAULT_PROXY_DOWNLOAD_THREAD_PRIORITY = Thread.MAX_PRIORITY //为保证体验，边下边播的下载线程优先级最高



    const val DEFAULT_PRE_DOWNLOAD_THREAD_POOL_SIZE = 1

    const val DEFAULT_PRE_DOWNLOAD_THREAD_PRIORITY = Thread.MIN_PRIORITY


    fun createCacheRootDir(context: Context?): File? {
        return createCacheRootDir(context, DIR_MEDIA_CACHE)
    }


    fun createCacheRootDir(context: Context?, name: String?): File? {
        return File(getDiskCacheDir(context!!), name)
    }


    fun createFileNameGenerator(): FileNameCreator? {
        return Md5FileNameCreator()
    }


    fun createExecutorService(threadPoolSize: Int, threadPriority: Int): ExecutorService? {
        val taskQueue: BlockingQueue<Runnable> = LinkedBlockingQueue()
        return ThreadPoolExecutor(
            threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, taskQueue,
            createThreadFactory(threadPriority, "medialoader-pool-")
        )
    }


    fun createPredownloadExecutorService(): ExecutorService? {
        return createExecutorService(
            DEFAULT_PRE_DOWNLOAD_THREAD_POOL_SIZE,
            DEFAULT_PRE_DOWNLOAD_THREAD_PRIORITY
        )
    }


    fun createCleanupExecutorService(): ExecutorService? {
        return Executors.newSingleThreadExecutor(
            createThreadFactory(
                Thread.NORM_PRIORITY,
                "medialoader-pool-cleanup-"
            )
        )
    }

    private fun createThreadFactory(threadPriority: Int, threadNamePrefix: String): ThreadFactory? {
        return DefaultThreadFactory(threadPriority, threadNamePrefix)
    }

    private class DefaultThreadFactory internal constructor(
        private val threadPriority: Int,
        threadNamePrefix: String
    ) :
        ThreadFactory {
        private val group: ThreadGroup
        private val threadNumber = AtomicInteger(1)
        private val namePrefix: String
        override fun newThread(r: Runnable): Thread {
            val t = Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0)
            if (t.isDaemon) t.isDaemon = false
            t.priority = threadPriority
            return t
        }

        companion object {
            private val poolNumber = AtomicInteger(1)
        }

        init {
            group = Thread.currentThread().threadGroup
            namePrefix = threadNamePrefix + poolNumber.getAndIncrement() + "-thread-"
        }
    }
}