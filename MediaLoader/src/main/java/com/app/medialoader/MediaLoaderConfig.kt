package com.app.medialoader

import android.content.Context
import com.app.medialoader.data.DefaultDataSourceFactory
import com.app.medialoader.data.file.naming.FileNameCreator
import com.app.medialoader.utils.Util
import java.io.File
import java.util.concurrent.ExecutorService

class MediaLoaderConfig(builder: Builder) {
    var context = builder.context
    var cacheRootDir = builder.cacheRootDir
    var cacheFileNameGenerator = builder.cacheFileNameGenerator
    var maxCacheFilesSize = builder.maxCacheFilesSize
    var maxCacheFilesCount = builder.maxCacheFilesCount
    var diskLruCache = DefaultDataSourceFactory.createDiskLruCache(this)
    var downloadThreadPoolSize = builder.downloadThreadPoolSize
    var downloadThreadPriority = builder.downloadThreadPriority
    var downloadExecutorService = builder.downloadExecutorService


    fun getBuilder(context: Context)= Builder(context)

    class Builder(mContext: Context){
        val context=mContext.applicationContext

        var cacheRootDir: File? = null

        var cacheFileNameGenerator: FileNameCreator? = null

        var maxCacheFilesSize = DefaultConfigFactory.DEFAULT_MAX_CACHE_FILES_SIZE

        var maxCacheFilesCount = DefaultConfigFactory.DEFAULT_MAX_CACHE_FILES_COUNT

        var maxCacheFileTimeLimit = DefaultConfigFactory.DEFAULT_MAX_CACHE_FILE_TIME_LIMIT

        var downloadThreadPoolSize =
            DefaultConfigFactory.DEFAULT_PROXY_DOWNLOAD_THREAD_POOL_SIZE

        var downloadThreadPriority =
            DefaultConfigFactory.DEFAULT_PROXY_DOWNLOAD_THREAD_PRIORITY

        var downloadExecutorService: ExecutorService? = null

        fun cacheRootDir(file: File): Builder {
            cacheRootDir = Util.notEmpty(file)
            return this
        }

        fun cacheFileNameGenerator(fileNameCreator: FileNameCreator): Builder {
            cacheFileNameGenerator = Util.notEmpty(fileNameCreator)
            return this
        }

        fun maxCacheFilesSize(size: Long): Builder {
            maxCacheFilesSize = size
            return this
        }

        fun maxCacheFilesCount(count: Int): Builder {
            maxCacheFilesCount = count
            return this
        }

        fun maxCacheFileTimeLimit(timeLimit: Long): Builder {
            maxCacheFileTimeLimit = timeLimit
            return this
        }

        fun downloadThreadPoolSize(threadPoolSize: Int): Builder {
            downloadThreadPoolSize = threadPoolSize
            return this
        }

        fun downloadThreadPriority(threadPriority: Int): Builder {
            downloadThreadPriority = if (threadPriority < Thread.MIN_PRIORITY) {
                Thread.MIN_PRIORITY
            } else {
                if (threadPriority > Thread.MAX_PRIORITY) {
                    Thread.MAX_PRIORITY
                } else {
                    threadPriority
                }
            }
            return this
        }

        fun downloadExecutorService(executorService: ExecutorService?): Builder {
            downloadExecutorService = executorService
            return this
        }

        fun build(): MediaLoaderConfig {
            initNullFieldsWithDefault()
            return MediaLoaderConfig(this)
        }

        private fun initNullFieldsWithDefault() {
            if (cacheRootDir == null) {
                cacheRootDir = DefaultConfigFactory.createCacheRootDir(context)
            }
            if (cacheFileNameGenerator == null) {
                cacheFileNameGenerator = DefaultConfigFactory.createFileNameGenerator()
            }
            if (downloadExecutorService == null) {
                downloadExecutorService = DefaultConfigFactory.createExecutorService(
                    downloadThreadPoolSize,
                    downloadThreadPriority
                )
            }
        }
    }
}