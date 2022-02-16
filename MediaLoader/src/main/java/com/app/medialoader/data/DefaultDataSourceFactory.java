package com.app.medialoader.data;

import com.app.medialoader.MediaLoaderConfig;
import com.app.medialoader.data.file.FileDataSource;
import com.app.medialoader.data.file.RandomAcessFileDataSource;
import com.app.medialoader.data.file.cleanup.DiskLruCache;
import com.app.medialoader.data.file.cleanup.SimpleDiskLruCache;
import com.app.medialoader.data.url.DefaultUrlDataSource;
import com.app.medialoader.data.url.UrlDataSource;

import java.io.File;

/**
 * 数据源默认生产工厂
 *
 * @author vincanyang
 */
public final class DefaultDataSourceFactory {

    public static UrlDataSource createUrlDataSource(String url) {
        return new DefaultUrlDataSource(url);
    }

    public static UrlDataSource createUrlDataSource(DefaultUrlDataSource dataSource) {
        return new DefaultUrlDataSource(dataSource);
    }

    public static FileDataSource createFileDataSource(File file, DiskLruCache diskLruStorage) {
        return new RandomAcessFileDataSource(file, diskLruStorage);
    }

    public static DiskLruCache createDiskLruCache(MediaLoaderConfig mediaLoaderConfig) {
        return new SimpleDiskLruCache(mediaLoaderConfig);
    }
}
