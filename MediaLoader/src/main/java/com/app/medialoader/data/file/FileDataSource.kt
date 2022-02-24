package com.app.medialoader.data.file

import java.io.Closeable
import java.io.File
import java.io.IOException
import kotlin.Throws

/**
 * 文件数据源接口，
 * <br></br>
 * 注意：读和写会在不同线程访问，需要支持多线程
 *
 * @author vincanyang
 */
interface FileDataSource : Closeable {
    @Throws(IOException::class)
    fun length(): Long

    @Throws(IOException::class)
    fun seekAndRead(offset: Long, buffer: ByteArray?): Int

    @Throws(IOException::class)
    fun append(data: ByteArray?, length: Int)

    @Throws(IOException::class)
    fun complete()
    val isCompleted: Boolean
    val file: File?
}