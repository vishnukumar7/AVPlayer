package com.app.avplayer.utils

import androidx.fragment.app.Fragment

class Constants {

    companion object{
        var TAG_DATA = "data"
        var TAG_FROM = "from"
        var TAG_POSITION = "position"
        var TAG_ALBUM_ID = "album_id"
        var TAG_TITLE = "title"
        var TAG_IMAGES = "images"
        var TAG_SONG_PATH = "song_path"

        var PATH=""
        var currentFragment:Fragment?=null

        fun getSize(data: Long): String {
            var values = data
            if (values < 1024) {
                return "$values B"
            } else {
                values /= 1024
                if (values < 1024) {
                    return "$values KB"
                } else {
                    values /= 1024
                    if (values < 1024) {
                        return "$values MB"
                    } else {
                        values /= 1024
                        return "$values GB"
                    }
                }
            }
        }

        fun clockLength(data: Long, video: Boolean): String {
            var total = data / 1000
            val secs = total % 60
            total /= 60
            val mins = total % 60
            total /= 60
            val hrs = total
            if (video)
                return "$hrs:$mins:$secs"
            else
                return "$mins:$secs"

        }
    }
}