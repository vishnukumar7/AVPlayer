package com.app.medialoader.data.file

import com.app.medialoader.data.file.cleanup.DiskLruCache
import com.app.medialoader.utils.FileUtil.mkdirs
import com.app.medialoader.utils.LogUtil.e
import java.io.File
import java.io.IOException

/**
 * [FileDataSource]的通用实现
 *
 * @author vincanyang
 */
abstract class BaseFileDataSource(
    protected var mOriginFile: File,
    private var mDiskLruStorage: DiskLruCache?
) : FileDataSource {
    @Throws(IOException::class)
    override fun close() {
        mDiskLruStorage?.save("", mOriginFile)
    }

    init {
        try {
            mkdirs(mOriginFile.parentFile)
        } catch (e: IOException) {
            e(e)
        }
    }
}