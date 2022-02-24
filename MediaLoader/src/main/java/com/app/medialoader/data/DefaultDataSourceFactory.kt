package com.app.medialoader.data

import com.app.medialoader.MediaLoaderConfig
import com.app.medialoader.data.file.FileDataSource
import com.app.medialoader.data.file.RandomAcessFileDataSource
import com.app.medialoader.data.file.cleanup.DiskLruCache
import com.app.medialoader.data.file.cleanup.SimpleDiskLruCache
import com.app.medialoader.data.url.DefaultUrlDataSource
import com.app.medialoader.data.url.UrlDataSource
import java.io.File

object DefaultDataSourceFactory {

    fun createUrlDataSource(url: String?): UrlDataSource {
        return DefaultUrlDataSource(url)
    }

    fun createUrlDataSource(dataSource: DefaultUrlDataSource): UrlDataSource {
        return DefaultUrlDataSource(dataSource)
    }

    fun createFileDataSource(file: File, diskLruStorage: DiskLruCache?): FileDataSource {
        return RandomAcessFileDataSource(file, diskLruStorage)
    }

    fun createDiskLruCache(mediaLoaderConfig: MediaLoaderConfig): DiskLruCache {
        return SimpleDiskLruCache(mediaLoaderConfig)
    }
}