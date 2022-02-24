package com.app.medialoader.data.file.naming

import com.app.medialoader.utils.Util.getExtensionFromUrl
import com.app.medialoader.utils.Util.getMD5
import com.app.medialoader.data.file.naming.FileNameCreator
import android.text.TextUtils

class HashCodeFileNameCreator : FileNameCreator {
    override fun create(url: String): String? {
        return url.hashCode().toString()
    }
}