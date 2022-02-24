package com.app.avplayer.utils

import android.content.Context
import androidx.fragment.app.Fragment

object Constants {

    var TAG_DATA = "data"
    var TAG_FROM = "from"
    var TAG_POSITION = "position"
    var TAG_ALBUM_ID = "album_id"
    var TAG_TITLE = "title"
    var TAG_IMAGES = "images"
    var TAG_SONG_PATH = "song_path"

    var PATH=""
    var currentFragment:Fragment?=null

    const val DOCUMENT_TYPE=1
    const val AUDIO_TYPE=2
    const val VIDEO_TYPE=3
    const val GALLERY_TYPE=4
    const val ALBUM_TYPE=5
    const val FILES_TYPE=6
    const val IMAGE_TYPE=7

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

    fun isUpdated(context: Context,timeStamp: Long): Boolean{
        val pref=context.getSharedPreferences("player_pref",Context.MODE_PRIVATE)
        val delay=1000*60*60*6
        var lastUpdated=pref.getLong("updated",0)
        lastUpdated += delay
        return lastUpdated<timeStamp
    }

    fun setUpdatedTime(context: Context,timeStamp: Long){
        val pref=context.getSharedPreferences("player_pref",Context.MODE_PRIVATE)
        val editor=pref.edit()
        editor.putLong("updated",timeStamp)
        editor.apply()
    }
}