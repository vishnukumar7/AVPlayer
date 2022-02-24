package com.app.medialoader.data.file.naming

import com.app.medialoader.utils.Util.getExtensionFromUrl
import com.app.medialoader.utils.Util.getMD5
import com.app.medialoader.data.file.naming.FileNameCreator
import android.text.TextUtils


class Md5FileNameCreator : FileNameCreator {
    override fun create(url: String): String? {
        val extension = getExtensionFromUrl(url)
        val name = getMD5(url)
        return if (TextUtils.isEmpty(extension)) name else "$name.$extension"
    }
}