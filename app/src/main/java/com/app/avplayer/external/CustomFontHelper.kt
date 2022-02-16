package com.app.avplayer.external

import android.content.Context

import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.EditText

import android.widget.TextView
import com.app.avplayer.R


class CustomFontHelper {
    companion object{
        fun setCustomFont(textview: TextView, context: Context?, attrs: AttributeSet?) {
            val a: TypedArray? = context?.obtainStyledAttributes(attrs, R.styleable.CustomFont)
            val font = a?.getString(R.styleable.CustomFont_typeface)
            setCustomFont(textview, font, context)
            a?.recycle()
        }


        fun setCustomFont(checkBox: CheckBox, context: Context?, attrs: AttributeSet) {
            val a = context?.obtainStyledAttributes(attrs, R.styleable.CustomFont)
            val font = a?.getString(R.styleable.CustomFont_typeface)
            setCustomFont(checkBox, font, context)
            a?.recycle()
        }

        fun setCustomFont(checkBox: CheckBox, font: String?, context: Context?) {
            if (font == null) {
                return
            }
            val tf: Typeface? = FontCache[font, context]
            if (tf != null) {
                checkBox.typeface = tf
            }
        }

        fun setCustomFont(textview: TextView, font: String?, context: Context?) {
            if (font == null) {
                return
            }
            val tf = FontCache[font, context]
            if (tf != null) {
                textview.typeface = tf
            }
        }

        fun setCustomFont(textview: EditText, context: Context, attrs: AttributeSet?) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.CustomFont)
            val font = a.getString(R.styleable.CustomFont_typeface)
            setCustomFont(textview, font, context)
            a.recycle()
        }

        fun setCustomFont(textview: EditText, font: String?, context: Context?) {
            if (font == null) {
                return
            }
            val tf = FontCache[font, context]
            if (tf != null) {
                textview.typeface = tf
            }
        }
    }
}