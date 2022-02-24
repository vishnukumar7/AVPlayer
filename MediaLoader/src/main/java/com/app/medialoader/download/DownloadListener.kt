package com.app.medialoader.download

import java.io.File

/**
 * 下载监听器
 *
 * @author vincanyang
 */
interface DownloadListener {
    fun onProgress(url: String?, file: File?, progress: Int)
    fun onError(e: Throwable?)
}