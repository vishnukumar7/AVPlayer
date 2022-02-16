package com.app.avplayer.external


import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import com.app.avplayer.R


class RoundedImageView : AppCompatImageView {
    private var mCornerRadius = DEFAULT_RADIUS
    private var mBorderWidth = DEFAULT_BORDER_WIDTH
    private var mBorderColor: ColorStateList? =
        ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
    private var mOval = false
    private var mRoundBackground = false
    private var mResource = 0
    private var mDrawable: Drawable? = null
    private var mBackgroundDrawable: Drawable? = null
    private var mScaleType: ScaleType? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        val a: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, defStyle, 0)
        val index = a.getInt(R.styleable.RoundedImageView_android_scaleType, -1)
        scaleType = if (index >= 0) {
            SCALE_TYPES[index]
        } else {
            // default scaletype to FIT_CENTER
            ScaleType.FIT_CENTER
        }
        mCornerRadius = a.getDimensionPixelSize(R.styleable.RoundedImageView_corner_radius, -1)
        mBorderWidth = a.getDimensionPixelSize(R.styleable.RoundedImageView_border_width, -1)

        // don't allow negative values for radius and border
        if (mCornerRadius < 0) {
            mCornerRadius = DEFAULT_RADIUS
        }
        if (mBorderWidth < 0) {
            mBorderWidth = DEFAULT_BORDER_WIDTH
        }
        mBorderColor = a.getColorStateList(R.styleable.RoundedImageView_border_color)
        if (mBorderColor == null) {
            mBorderColor = ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
        }
        mRoundBackground = a.getBoolean(R.styleable.RoundedImageView_round_background, false)
        mOval = a.getBoolean(R.styleable.RoundedImageView_is_oval, false)
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs()
        a.recycle()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        invalidate()
    }

    /**
     * Return the current scale type in use by this ImageView.
     *
     * @attr ref android.R.styleable#ImageView_scaleType
     * @see ScaleType
     */
    override fun getScaleType(): ScaleType {
        return mScaleType!!
    }

    /**
     * Controls how the image should be resized or moved to match the size
     * of this ImageView.
     *
     * @param scaleType The desired scaling mode.
     * @attr ref android.R.styleable#ImageView_scaleType
     */
    override fun setScaleType(scaleType: ScaleType) {
        if (scaleType == null) {
            throw NullPointerException()
        }
        if (mScaleType != scaleType) {
            mScaleType = scaleType
            when (scaleType) {
                ScaleType.CENTER, ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE, ScaleType.FIT_CENTER, ScaleType.FIT_START, ScaleType.FIT_END, ScaleType.FIT_XY -> super.setScaleType(
                    ScaleType.FIT_XY
                )
                else -> super.setScaleType(scaleType)
            }
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs()
            invalidate()
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        mResource = 0
        mDrawable = RoundedDrawable.fromDrawable(drawable)
        updateDrawableAttrs()
        super.setImageDrawable(mDrawable)
    }

    override fun setImageBitmap(bm: Bitmap) {
        mResource = 0
        mDrawable = RoundedDrawable.fromBitmap(bm)
        updateDrawableAttrs()
        super.setImageDrawable(mDrawable)
    }

    override fun setImageResource(resId: Int) {
        if (mResource != resId) {
            mResource = resId
            mDrawable = resolveResource()
            updateDrawableAttrs()
            super.setImageDrawable(mDrawable)
        }
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        setImageDrawable(drawable)
    }

    private fun resolveResource(): Drawable? {
        val rsrc = resources ?: return null
        var d: Drawable? = null
        if (mResource != 0) {
            try {
                d = rsrc.getDrawable(mResource)
            } catch (e: Exception) {
                Log.w(TAG, "Unable to find resource: $mResource", e)
                // Don't try again.
                mResource = 0
            }
        }
        return RoundedDrawable.fromDrawable(d)
    }

    override fun setBackground(background: Drawable) {
        super.setBackground(background)
    }

    private fun updateDrawableAttrs() {
        updateAttrs(mDrawable, false)
    }

    private fun updateBackgroundDrawableAttrs() {
        updateAttrs(mBackgroundDrawable, true)
    }

    private fun updateAttrs(drawable: Drawable?, background: Boolean) {
        if (drawable == null) {
            return
        }
        if (drawable is RoundedDrawable) {
            drawable
                .setScaleType(mScaleType)
                .setCornerRadius((if (background && !mRoundBackground) 0 else mCornerRadius).toFloat())
                .setBorderWidth(if (background && !mRoundBackground) 0 else mBorderWidth)
                .setBorderColors(mBorderColor)
                .setOval(mOval)
        } else if (drawable is LayerDrawable) {
            // loop through layers to and set drawable attrs
            val layers = drawable.numberOfLayers
            for (i in 0 until layers) {
                updateAttrs(drawable.getDrawable(i), background)
            }
        }
    }

    @Deprecated("")
    override fun setBackgroundDrawable(background: Drawable?) {
        mBackgroundDrawable = RoundedDrawable.fromDrawable(background)
        updateBackgroundDrawableAttrs()
        super.setBackgroundDrawable(mBackgroundDrawable)
    }

    var cornerRadius: Int
        get() = mCornerRadius
        set(radius) {
            if (mCornerRadius == radius) {
                return
            }
            mCornerRadius = radius
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs()
        }
    var borderWidth: Int
        get() = mBorderWidth
        set(width) {
            if (mBorderWidth == width) {
                return
            }
            mBorderWidth = width
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs()
            invalidate()
        }
    var borderColor: Int
        get() = mBorderColor!!.defaultColor
        set(color) {
            borderColors = ColorStateList.valueOf(color)
        }
    var borderColors: ColorStateList?
        get() = mBorderColor
        set(colors) {
            if (mBorderColor == colors) {
                return
            }
            mBorderColor = colors ?: ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs()
            if (mBorderWidth > 0) {
                invalidate()
            }
        }
    var isOval: Boolean
        get() = mOval
        set(oval) {
            mOval = oval
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs()
            invalidate()
        }
    var isRoundBackground: Boolean
        get() = mRoundBackground
        set(roundBackground) {
            if (mRoundBackground == roundBackground) {
                return
            }
            mRoundBackground = roundBackground
            updateBackgroundDrawableAttrs()
            invalidate()
        }

    companion object {
        const val TAG = "RoundedImageView"
        const val DEFAULT_RADIUS = 0
        const val DEFAULT_BORDER_WIDTH = 0
        private val SCALE_TYPES = arrayOf(
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
        )
    }
}