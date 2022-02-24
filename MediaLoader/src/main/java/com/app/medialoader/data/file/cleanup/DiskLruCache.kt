package com.app.medialoader.data.file.cleanup

import java.io.File

/**
 * 磁盘LRU缓存，使用LRU对磁盘文件进行清理
 *
 * @author vincanyang
 */
interface DiskLruCache {
    fun save(url: String?, file: File?)
    operator fun get(url: String?): File?
    fun remove(url: String?)
    fun close()
    fun clear()
}