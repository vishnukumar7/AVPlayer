package com.app.avplayer.external

import android.content.Context
import android.graphics.Typeface
import java.lang.Exception
import java.util.*

class FontCache {

    companion object{
        private val fontCache = Hashtable<String, Typeface?>()

        operator fun get(name: String, context: Context?): Typeface? {
            var tf = fontCache[name]
            if (tf == null) {
                tf = try {
                    Typeface.createFromAsset(context?.assets, name)
                } catch (e: Exception) {
                    return null
                }
                fontCache[name] = tf
            }
            return tf
        }
    }
}