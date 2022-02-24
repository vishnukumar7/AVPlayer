package com.app.medialoader.tinyhttpd

import android.text.TextUtils
import java.util.regex.Pattern

class HttpHeaders: LinkedHashMap<String, String>() {

    companion object{
        private val HEADER_RANGE_PATTERN = Pattern.compile("bytes=(\\d*)-")
    }

    interface Names {
        companion object {
            const val RANGE = "Range"
            const val ACCEPT_RANGES = "Accept-Ranges"
            const val CONTENT_LENGTH = "Content-Length"
            const val CONTENT_RANGE = "Content-Range"
            const val CONTENT_TYPE = "Content-Type"
        }
    }

    interface Values {
        companion object {
            const val BYTES = "bytes"
        }
    }

    private var mRangeOffset = Long.MIN_VALUE

    fun getRangeOffset(): Long {
        if (mRangeOffset == Long.MIN_VALUE) {
            val range = get(Names.RANGE)
            if (!TextUtils.isEmpty(range)) {
                val matcher = HEADER_RANGE_PATTERN.matcher(range)
                if (matcher.find()) {
                    val rangeValue = matcher.group(1)
                    mRangeOffset = rangeValue.toLong()
                }
            }
        }
        return Math.max(mRangeOffset, 0)
    }

    fun isPartial(): Boolean {
        return containsKey(Names.RANGE)
    }
}