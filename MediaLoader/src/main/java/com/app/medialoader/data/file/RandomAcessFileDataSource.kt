package com.app.medialoader.data.file

import com.app.medialoader.utils.LogUtil.e
import com.app.medialoader.data.file.BaseFileDataSource
import kotlin.Throws
import com.app.medialoader.data.file.RandomAcessFileDataSource
import com.app.medialoader.data.file.cleanup.DiskLruCache
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

/**
 * [RandomAccessFile]实现
 *
 * @author vincanyang
 */
class RandomAcessFileDataSource(file: File, diskLruStorage: DiskLruCache?) :
    BaseFileDataSource(file, diskLruStorage) {
    private var mRandomAccessFile: RandomAccessFile? = null
    @Synchronized
    @Throws(IOException::class)
    override fun length(): Long {
        return mRandomAccessFile!!.length()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun close() {
        super.close()
        mRandomAccessFile!!.close()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun seekAndRead(offset: Long, buffer: ByteArray?): Int {
        //两个操作必须在同一个synchronized里，计同一个线程里
        mRandomAccessFile!!.seek(offset)
        return mRandomAccessFile!!.read(buffer, 0, buffer!!.size)
    }

    @Synchronized
    @Throws(IOException::class)
    override fun append(data: ByteArray?, length: Int) {
        if (isCompleted) {
            return
        }
        mRandomAccessFile!!.seek(length())
        mRandomAccessFile!!.write(data, 0, length)
    }

    @Synchronized
    @Throws(IOException::class)
    override fun complete() {
        if (isCompleted) {
            return
        }
        close()
        renameCompletedFile()
    }

    @Throws(IOException::class)
    private fun renameCompletedFile() {
        val newFileName =
            mOriginFile.name.substring(0, mOriginFile.name.length - TEMP_POSTFIX.length)
        val completedFile = File(mOriginFile.parentFile, newFileName)
        val renamed = mOriginFile.renameTo(completedFile)
        if (!renamed) {
            throw IOException("Error renaming file $mOriginFile to $completedFile")
        }
        //refresh file
        mOriginFile = completedFile
        mRandomAccessFile = RandomAccessFile(mOriginFile, "r")
    }

    override val isCompleted: Boolean
        get() = !mOriginFile.name.endsWith(TEMP_POSTFIX)
    override val file: File
        get() = mOriginFile

    companion object {
        private const val TEMP_POSTFIX = ".tmp"
    }

    init {
        try {
            val completed = file.exists()
            mOriginFile = if (completed) file else File(file.parentFile, file.name + TEMP_POSTFIX)
            mRandomAccessFile = RandomAccessFile(mOriginFile, if (completed) "r" else "rw")
        } catch (e: IOException) {
            //should not happen
            e(e)
        }
    }
}