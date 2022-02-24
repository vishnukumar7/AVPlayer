package com.app.medialoader.data.url

abstract class BaseUrlDataSource : UrlDataSource {

    protected var mUrlDataSourceInfoCache: HashMap<String, UrlDataSourceInfo?> = HashMap()
}