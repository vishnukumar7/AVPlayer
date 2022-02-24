package com.app.medialoader.data.file.cleanup

import android.os.Process
import com.app.medialoader.DefaultConfigFactory
import com.app.medialoader.MediaLoaderConfig
import com.app.medialoader.utils.FileUtil
import com.app.medialoader.utils.Util
import java.io.File
import java.io.IOException
import java.util.concurrent.Executor

/**
 * 磁盘LRU缓存的简单实现
 *
 * @author vincanyang
 */
class SimpleDiskLruCache(mediaLoaderConfig: MediaLoaderConfig) : DiskLruCache {
    private val mCleanupExecutor: Executor = DefaultConfigFactory.createCleanupExecutorService()!!
    private val mMediaLoaderConfig: MediaLoaderConfig = mediaLoaderConfig
    override fun get(url: String?): File {
        val cacheDir: File? = mMediaLoaderConfig.cacheRootDir
        val fileName: String = mMediaLoaderConfig.cacheFileNameGenerator!!.create(Util.notEmpty(url))!!
        val file = File(cacheDir, fileName)
        mCleanupExecutor.execute(CleanupRunnable(file))
        return file
    }

    private fun cleanup(files: List<File?>?) {
        var totalFilesSize = countTotalSize(files)
        var totalFilesCount = files!!.size
        for (file in files) {
            val reserved =
                totalFilesCount <= mMediaLoaderConfig.maxCacheFilesCount && totalFilesSize <= mMediaLoaderConfig.maxCacheFilesSize

            if (!reserved) {
                val fileSize = file!!.length()
                val deleted = file.delete()
                if (deleted) {
                    totalFilesSize -= fileSize
                    totalFilesCount--
                }
            }
        }
    }

    private fun countTotalSize(files: List<File?>?): Long {
        var totalSize: Long = 0
        for (i in files!!.indices) {
            totalSize += files[i]!!.length()
        }
        return totalSize
    }

    private inner class CleanupRunnable(private val file: File) : Runnable {
        override fun run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND) //避免和主线程抢占资源发生ANR
            try {
                FileUtil.updateLastModified(file)
            } catch (e: IOException) {
            }
            cleanup(FileUtil.getLruListFiles(file.parentFile))
        }
    }

    override fun save(url: String?, file: File?) {}
    override fun remove(url: String?) {}
    override fun close() {}
    override fun clear() {}

}