package com.app.medialoader.data.file.naming


interface FileNameCreator {

    fun create(url: String): String?
}