package com.app.medialoader.data.file.cleanup

import com.app.medialoader.MediaLoaderConfig
import java.io.File

/**
 * 磁盘LRU缓存的sqlite实现
 * //TODO
 *
 * @author vincanyang
 */
class SqliteDiskLruCache(private val mMediaLoaderConfig: MediaLoaderConfig) : DiskLruCache {
    override fun save(url: String?, file: File?) {}
    override fun get(url: String?): File? {
        return null
    }

    override fun remove(url: String?) {}
    override fun close() {}
    override fun clear() {}
}