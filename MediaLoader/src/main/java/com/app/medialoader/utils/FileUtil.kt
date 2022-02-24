package com.app.medialoader.utils

import android.content.Context
import android.os.Environment
import com.app.medialoader.utils.Util.notEmpty
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*

object FileUtil {

    fun getDiskCacheDir(context: Context): File? {
        val cacheDir: File? =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
                context.externalCacheDir //sdcard/Android/data/${application package}/cache
            } else {
                context.cacheDir //data/data/${application package}/cache
            }
        return cacheDir
    }

    @Throws(IOException::class)
    fun mkdirs(dir: File) {
        if (!notEmpty(dir).exists()) {
            if (!dir.mkdirs()) {
                throw IOException(String.format("Error create directory %s", dir.absolutePath))
            }
        }
    }

    fun getLruListFiles(dir: File): List<File?>? {
        var lruListFiles: List<File?>? = LinkedList()
        val listFiles = dir.listFiles()
        if (listFiles != null) {
            lruListFiles = Arrays.asList(*listFiles)
            Collections.sort(lruListFiles) { lhs, rhs ->
                val first = lhs.lastModified()
                val second = rhs.lastModified()
                if (first < second) -1 else if (first == second) 0 else 1
            }
        }
        return lruListFiles
    }

    @Throws(IOException::class)
    fun updateLastModified(file: File) {
        if (file.exists()) {
            val isModified =
                file.setLastModified(System.currentTimeMillis()) //某些设备上setLastModified()会失效
            if (!isModified) {
                //ugly modify
                val raf = RandomAccessFile(file, "rw")
                val length = raf.length()
                raf.setLength(length + 1)
                raf.setLength(length)
                raf.close()
            }
        }
    }

    fun cleanDir(file: File) {
        if (!file.exists()) {
            return
        }
        if (file.isDirectory) {
            val listFile = file.listFiles()
            for (i in listFile.indices) {
                cleanDir(listFile[i])
                listFile[i].delete()
            }
        }
    }
}