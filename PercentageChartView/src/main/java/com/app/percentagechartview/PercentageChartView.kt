/*
 * Copyright 2018 Rami Jemli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.app.percentagechartview

import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import com.app.percentagechartview.annotation.*
import com.app.percentagechartview.callback.AdaptiveColorProvider
import com.app.percentagechartview.callback.OnProgressChangeListener
import com.app.percentagechartview.callback.ProgressTextFormatter
import com.app.percentagechartview.renderer.*

class PercentageChartView : View, IPercentageChartView {
    private var renderer: BaseModeRenderer? = null

    /**
     * Gets the percentage chart view mode.
     *
     * @return the percentage chart view mode
     */
    @get:ChartMode
    @ChartMode
    var mode = 0
        private set
    private var onProgressChangeListener: OnProgressChangeListener? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attributeSet: AttributeSet?) {
        if (attributeSet != null) {
            val attrs = context.theme.obtainStyledAttributes(
                attributeSet,
                R.styleable.PercentageChartView,
                0, 0
            )
            try {
                //CHART MODE (DEFAULT PIE MODE)
                mode = attrs.getInt(
                    R.styleable.PercentageChartView_pcv_mode,
                    BaseModeRenderer.MODE_PIE
                )
                renderer = when (mode) {
                    BaseModeRenderer.MODE_RING -> RingModeRenderer(this, attrs)
                    BaseModeRenderer.MODE_FILL -> FillModeRenderer(this, attrs)
                    BaseModeRenderer.MODE_PIE -> PieModeRenderer(this, attrs)
                    else -> PieModeRenderer(this, attrs)
                }
            } finally {
                attrs.recycle()
            }
        } else {
            mode = BaseModeRenderer.MODE_PIE
            renderer = PieModeRenderer(this)
        }
        isSaveEnabled = true
    }

    //##############################################################################################   BEHAVIOR
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        if (renderer != null) renderer!!.measure(
            w,
            h,
            paddingLeft,
            paddingTop,
            paddingRight,
            paddingBottom
        )
        setMeasuredDimension(w, h)
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        renderer!!.destroy()
        renderer = null
        if (onProgressChangeListener != null) {
            onProgressChangeListener = null
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        renderer!!.draw(canvas)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(STATE_SUPER_INSTANCE, super.onSaveInstanceState())
        bundle.putInt(STATE_MODE, mode)
        if (renderer is OrientationBasedMode) {
            bundle.putInt(STATE_ORIENTATION, (renderer as OrientationBasedMode).orientation)
        }
        bundle.putFloat(STATE_START_ANGLE, renderer!!.getStartAngle())
        bundle.putInt(STATE_DURATION, renderer!!.getAnimationDuration())
        bundle.putFloat(STATE_PROGRESS, renderer!!.getProgress())
        bundle.putInt(STATE_PG_COLOR, renderer!!.getProgressColor())
        bundle.putBoolean(STATE_DRAW_BG, renderer!!.isDrawBackgroundEnabled())
        bundle.putInt(STATE_BG_COLOR, renderer!!.getBackgroundColor())
        if (renderer is OffsetEnabledMode) {
            bundle.putInt(STATE_BG_OFFSET, (renderer as OffsetEnabledMode).backgroundOffset)
        }
        bundle.putInt(STATE_TXT_COLOR, renderer!!.getTextColor())
        bundle.putFloat(STATE_TXT_SIZE, renderer!!.getTextSize())
        bundle.putInt(STATE_TXT_SHA_COLOR, renderer!!.getTextShadowColor())
        bundle.putFloat(STATE_TXT_SHA_RADIUS, renderer!!.getTextShadowRadius())
        bundle.putFloat(STATE_TXT_SHA_DIST_X, renderer!!.getTextShadowDistX())
        bundle.putFloat(STATE_TXT_SHA_DIST_Y, renderer!!.getTextShadowDistY())
        if (renderer is RingModeRenderer) {
            bundle.putFloat(
                STATE_PG_BAR_THICKNESS,
                (renderer as RingModeRenderer).progressBarThickness
            )
            bundle.putInt(STATE_PG_BAR_STYLE, (renderer as RingModeRenderer).progressBarStyle)
            bundle.putBoolean(
                STATE_DRAW_BG_BAR,
                (renderer as RingModeRenderer).isDrawBackgroundBarEnabled
            )
            bundle.putInt(STATE_BG_BAR_COLOR, (renderer as RingModeRenderer).backgroundBarColor)
            bundle.putFloat(
                STATE_BG_BAR_THICKNESS,
                (renderer as RingModeRenderer).backgroundBarThickness
            )
        }
        if (renderer!!.getGradientType() != -1) {
            bundle.putInt(STATE_GRADIENT_TYPE, renderer!!.getGradientType())
            bundle.putFloat(STATE_GRADIENT_ANGLE, renderer!!.getGradientAngle())
            bundle.putIntArray(STATE_GRADIENT_COLORS, renderer!!.getGradientColors())
            bundle.putFloatArray(STATE_GRADIENT_POSITIONS, renderer!!.getGradientDistributions())
        }
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            val bundle = state
            mode = bundle.getInt(STATE_MODE)
            when (mode) {
                BaseModeRenderer.MODE_RING -> {
                    renderer = RingModeRenderer(this)
                    (renderer as RingModeRenderer).progressBarThickness =
                        bundle.getFloat(STATE_PG_BAR_THICKNESS)
                    (renderer as RingModeRenderer).progressBarStyle = bundle.getInt(
                        STATE_PG_BAR_STYLE
                    )
                    (renderer as RingModeRenderer).isDrawBackgroundBarEnabled =
                        bundle.getBoolean(STATE_DRAW_BG_BAR)
                    (renderer as RingModeRenderer).backgroundBarColor = bundle.getInt(
                        STATE_BG_BAR_COLOR
                    )
                    (renderer as RingModeRenderer).backgroundBarThickness =
                        bundle.getFloat(STATE_BG_BAR_THICKNESS)
                }
                BaseModeRenderer.MODE_FILL -> renderer = FillModeRenderer(this)
                BaseModeRenderer.MODE_PIE -> renderer = PieModeRenderer(this)
                else -> renderer = PieModeRenderer(this)
            }
            if (renderer is OrientationBasedMode) {
                (renderer as OrientationBasedMode).orientation =
                    bundle.getInt(STATE_ORIENTATION)
            }
            renderer!!.setStartAngle(bundle.getFloat(STATE_START_ANGLE))
            renderer!!.setAnimationDuration(bundle.getInt(STATE_DURATION))
            renderer!!.setProgress(bundle.getFloat(STATE_PROGRESS), false)
            renderer!!.setProgressColor(bundle.getInt(STATE_PG_COLOR))
            renderer!!.setDrawBackgroundEnabled(bundle.getBoolean(STATE_DRAW_BG))
            renderer!!.setBackgroundColor(bundle.getInt(STATE_BG_COLOR))
            if (renderer is OffsetEnabledMode) {
                (renderer as OffsetEnabledMode).backgroundOffset = bundle.getInt(STATE_BG_OFFSET)
            }
            renderer!!.setTextColor(bundle.getInt(STATE_TXT_COLOR))
            renderer!!.setTextSize(bundle.getFloat(STATE_TXT_SIZE))
            renderer!!.setTextShadow(
                bundle.getInt(STATE_TXT_SHA_COLOR),
                bundle.getFloat(STATE_TXT_SHA_RADIUS),
                bundle.getFloat(STATE_TXT_SHA_DIST_X),
                bundle.getFloat(STATE_TXT_SHA_DIST_Y)
            )
            if (bundle.getInt(STATE_GRADIENT_TYPE, -1) != -1) {
                bundle.getIntArray(STATE_GRADIENT_COLORS)?.let {
                    bundle.getFloatArray(STATE_GRADIENT_POSITIONS)?.let { it1 ->
                        renderer!!.setGradientColorsInternal(
                            bundle.getInt(STATE_GRADIENT_TYPE),
                            it,
                            it1,
                            bundle.getFloat(STATE_GRADIENT_ANGLE)
                        )
                    }
                }
            }
            super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER_INSTANCE))
            return
        }
        super.onRestoreInstanceState(state)
    }

    override fun getViewContext(): Context {
        return context
    }
    override fun onProgressUpdated(progress: Float) {
        if (onProgressChangeListener != null) onProgressChangeListener!!.onProgressChanged(progress)
    }
    //##############################################################################################   STYLE MODIFIERS
    /**
     * Gets the current drawing orientation.
     *
     * @return the current drawing orientation
     */
    /**
     * Sets the circular drawing direction. Default orientation is ORIENTATION_CLOCKWISE.
     *
     * @param orientation non-negative orientation constant.
     */
    @get:ProgressOrientation
    var orientation: Int
        get() = if (renderer !is OrientationBasedMode) BaseModeRenderer.INVALID_ORIENTATION else (renderer as OrientationBasedMode).orientation
        set(orientation) {
            orientation(orientation)
            postInvalidate()
        }
    /**
     * Gets the current circular drawing's start angle.
     *
     * @return the current circular drawing's start angle
     */
    /**
     * Sets the current circular drawing's start angle in degrees. Default start angle is0.
     *
     * @param startAngle A positive start angle value that is less or equal to 360.
     */
    @get:FloatRange(from = 0.0, to = 360.0)
    var startAngle: Float
        get() = renderer!!.getStartAngle()
        set(startAngle) {
            startAngle(startAngle)
            postInvalidate()
        }
    /**
     * Gets whether drawing background has been enabled.
     *
     * @return whether drawing background has been enabled
     */
    /**
     * Sets whether background should be drawn.
     *
     * @param enabled True if background have to be drawn, false otherwise.
     */
    var isDrawBackgroundEnabled: Boolean
        get() = renderer!!.isDrawBackgroundEnabled()
        set(enabled) {
            drawBackgroundEnabled(enabled)
            postInvalidate()
        }

    /**
     * Gets the circular background color for this view.
     *
     * @return the color of the circular background
     */
    @ColorInt
    fun getBackgroundColor(): Int {
        return renderer!!.getBackgroundColor()
    }

    /**
     * Sets the circular background color for this view.
     *
     * @param color the color of the circular background
     */
    override fun setBackgroundColor(@ColorInt color: Int) {
        backgroundColor(color)
        postInvalidate()
    }

    /**
     * Gets the current progress.
     *
     * @return the current progress
     */
    @get:FloatRange(from = 0.0, to = 100.0
    )
    val progress: Float
        get() = renderer!!.getProgress()

    /**
     * Sets a new progress value. Passing true in animate will cause an animated progress update.
     *
     * @param progress New progress float value to set.
     * @param animate  Animation boolean value to set whether to animate progress change or not.
     * @throws IllegalArgumentException if the given progress is negative, or, less or equal to 100.
     */
    fun setProgress(@FloatRange(from = 0.0, to = 100.0) progress: Float, animate: Boolean) {
        require(!(progress < 0 || progress > 100)) { "Progress value must be positive and less or equal to 100." }
        renderer!!.setProgress(progress, animate)
    }
    /**
     * Gets the progress/progress bar color for this view.
     *
     * @return the progress/progress bar color.
     */
    /**
     * Sets the progress/progress bar color for this view.
     *
     * @param color the color of the progress/progress bar
     */
    @get:ColorInt
    var progressColor: Int
        get() = renderer!!.getProgressColor()
        set(color) {
            progressColor(color)
            postInvalidate()
        }

    /**
     * Gets progress gradient type.
     *
     * @return Gets progress gradient type.
     */
    @get:GradientTypes
    val gradientType: Int
        @SuppressLint("WrongConstant")
        get() = renderer!!.getGradientType()

    /**
     * Sets progress gradient colors.
     *
     * @param type      The gradient type which is a GradientTypes constant
     * @param colors    The colors to be distributed.
     * There must be at least 2 colors in the array.
     * @param positions May be NULL. The relative position of
     * each corresponding color in the colors array, beginning
     * with 0 and ending with 1.0. If the values are not
     * monotonic, the drawing may produce unexpected results.
     * If positions is NULL, then the colors are automatically
     * spaced evenly.
     * @param angle     Defines the direction for linear gradient type.
     */
    fun setGradientColors(
        @GradientTypes type: Int,
        colors: IntArray?,
        positions: FloatArray?,
        @FloatRange(from = 0.0, to = 360.0) angle: Float
    ) {
        gradientColors(type, colors, positions, angle)
        postInvalidate()
    }
    /**
     * Gets the duration of the progress change's animation.
     *
     * @return the duration of the progress change's animation
     */
    /**
     * Sets the duration of the progress change's animation.
     *
     * @param duration non-negative duration value.
     */
    @get:IntRange(from = 0)
    var animationDuration: Int
        get() = renderer!!.getAnimationDuration()
        set(duration) {
            animationDuration(duration)
        }
    /**
     * Gets the interpolator of the progress change's animation.
     *
     * @return the interpolator of the progress change's animation
     */
    /**
     * Sets the interpolator of the progress change's animation.
     *
     * @param interpolator TimeInterpolator instance.
     */
    var animationInterpolator: TimeInterpolator?
        get() = renderer!!.getAnimationInterpolator()
        set(interpolator) {
            interpolator?.let { animationInterpolator(it) }
        }
    /**
     * Gets the text color.
     *
     * @return the text color
     */
    /**
     * Sets the text color for this view.
     *
     * @param color the text color
     */
    @get:ColorInt
    var textColor: Int
        get() = renderer!!.getTextColor()
        set(color) {
            textColor(color)
            postInvalidate()
        }
    /**
     * Gets the text size.
     *
     * @return the text size
     */
    /**
     * Sets the text size.
     *
     * @param size the text size
     */
    var textSize: Float
        get() = renderer!!.getTextSize()
        set(size) {
            textSize(size)
            postInvalidate()
        }
    /**
     * Gets the text font.
     *
     * @return the text typeface
     */
    /**
     * Sets the text font.
     *
     * @param typeface the text font as a Typeface instance
     */
    var typeface: Typeface?
        get() = renderer!!.getTypeface()
        set(typeface) {
            typeface?.let { typeface(it) }
            postInvalidate()
        }
    /**
     * Gets the text style.
     *
     * @return the text style
     */
    /**
     * Sets the text style.
     *
     * @param style the text style.
     */
    @get:TextStyle
    var textStyle: Int
        @SuppressLint("WrongConstant")
        get() = renderer!!.getTextStyle()
        set(style) {
            textStyle(style)
            postInvalidate()
        }

    /**
     * Gets the text shadow color.
     *
     * @return the text shadow color
     */
    @get:ColorInt
    val textShadowColor: Int
        get() = renderer!!.getTextShadowColor()

    /**
     * Gets the text shadow radius.
     *
     * @return the text shadow radius
     */
    val textShadowRadius: Float
        get() = renderer!!.getTextShadowRadius()

    /**
     * Gets the text shadow y-axis distance.
     *
     * @return the text shadow y-axis distance
     */
    val textShadowDistY: Float
        get() = renderer!!.getTextShadowDistY()

    /**
     * Gets the text shadow x-axis distance.
     *
     * @return the text shadow x-axis distance
     */
    val textShadowDistX: Float
        get() = renderer!!.getTextShadowDistX()

    /**
     * Sets the text shadow. Passing zeros will remove the shadow.
     *
     * @param shadowColor  text shadow color value.
     * @param shadowRadius text shadow radius.
     * @param shadowDistX  text shadow y-axis distance.
     * @param shadowDistY  text shadow x-axis distance.
     */
    fun setTextShadow(
        @ColorInt shadowColor: Int,
        @FloatRange(from = 0.0) shadowRadius: Float,
        @FloatRange(from = 0.0) shadowDistX: Float,
        @FloatRange(from = 0.0) shadowDistY: Float
    ) {
        textShadow(shadowColor, shadowRadius, shadowDistX, shadowDistY)
        postInvalidate()
    }

    /**
     * Gets the offset of the circular background.
     *
     * @return the offset of the circular background.-1 if chart mode is not set to pie.
     */
    val backgroundOffset: Float
        get() = if (renderer !is OffsetEnabledMode) (-1).toFloat() else (renderer as OffsetEnabledMode).backgroundOffset.toFloat()

    /**
     * Sets the offset of the circular background. Works only if chart mode is set to pie.
     *
     * @param offset A positive offset value.
     */
    fun setBackgroundOffset(@androidx.annotation.IntRange(from = 0) offset: Int) {
        backgroundOffset(offset)
        postInvalidate()
    }
    /**
     * Gets whether drawing the background bar has been enabled.
     *
     * @return whether drawing the background bar has been enabled
     */
    /**
     * Sets whether background bar should be drawn.
     *
     * @param enabled True if background bar have to be drawn, false otherwise.
     */
    var isDrawBackgroundBarEnabled: Boolean
        get() = if (renderer !is RingModeRenderer) false else (renderer as RingModeRenderer).isDrawBackgroundBarEnabled
        set(enabled) {
            drawBackgroundBarEnabled(enabled)
            postInvalidate()
        }
    /**
     * Gets the background bar color.
     *
     * @return the background bar color. -1 if chart mode is not set to ring.
     */
    /**
     * Sets the background bar color.
     *
     * @param color the background bar color
     */
    var backgroundBarColor: Int
        get() = if (renderer !is RingModeRenderer) -1 else (renderer as RingModeRenderer).backgroundBarColor
        set(color) {
            backgroundBarColor(color)
            postInvalidate()
        }
    /**
     * Gets the background bar thickness in pixels.
     *
     * @return the background bar thickness in pixels. -1 if chart mode is not set to ring.
     */
    /**
     * Sets the background bar thickness in pixels. Works only if chart mode is set to ring.
     *
     * @param thickness non-negative thickness value in pixels.
     */
    var backgroundBarThickness: Float
        get() = if (renderer !is RingModeRenderer) (-1).toFloat() else (renderer as RingModeRenderer).backgroundBarThickness
        set(thickness) {
            backgroundBarThickness(thickness)
            postInvalidate()
        }
    /**
     * Gets the progress bar thickness in pixels.
     *
     * @return the progress bar thickness in pixels. -1 if chart mode is not set to ring.
     */
    /**
     * Sets the progress bar thickness in pixels. Works only if chart mode is set to ring.
     *
     * @param thickness non-negative thickness value in pixels.
     */
    var progressBarThickness: Float
        get() = if (renderer !is RingModeRenderer) (-1).toFloat() else (renderer as RingModeRenderer).progressBarThickness
        set(thickness) {
            progressBarThickness(thickness)
            postInvalidate()
        }
    /**
     * Gets the progress bar stroke style.
     *
     * @return the progress bar stroke style. -1 if chart mode is not set to ring.
     */
    /**
     * Sets the progress bar stroke style. Works only if chart mode is set to ring.
     *
     * @param style Progress bar stroke style as a ProgressStyle constant.
     */
    var progressBarStyle: Int
        get() = if (renderer !is RingModeRenderer) -1 else (renderer as RingModeRenderer).progressBarStyle
        set(style) {
            progressBarStyle(style)
            postInvalidate()
        }
    //############################################################################################## UPDATE PIPELINE AS A FLUENT API
    /**
     * Sets the circular drawing direction. Default orientation is ORIENTATION_CLOCKWISE.
     *
     * @param orientation non-negative orientation constant.
     * @throws IllegalArgumentException if the given orientation is not a ProgressOrientation constant or not supported by the current used chart mode.
     */
    fun orientation(@ProgressOrientation orientation: Int): PercentageChartView {
        require(!(orientation != BaseModeRenderer.ORIENTATION_CLOCKWISE && orientation != BaseModeRenderer.ORIENTATION_COUNTERCLOCKWISE)) { "Orientation must be a ProgressOrientation constant." }
        try {
            (renderer as OrientationBasedMode?)!!.orientation = orientation
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("Orientation is not support by the used percentage chart mode.")
        }
        return this
    }

    /**
     * Sets the current circular drawing's start angle in degrees. Default start angle is0.
     *
     * @param startAngle A positive start angle value that is less or equal to 360.
     * @throws IllegalArgumentException if the given start angle is not positive, or, less or equal to 360.
     */
    fun startAngle(@FloatRange(from = 0.0, to = 360.0) startAngle: Float): PercentageChartView {
        require(!(startAngle < 0 || startAngle > 360)) { "Start angle value must be positive and less or equal to 360." }
        renderer!!.setStartAngle(startAngle)
        return this
    }

    /**
     * Sets whether background should be drawn.
     *
     * @param enabled True if background have to be drawn, false otherwise.
     */
    fun drawBackgroundEnabled(enabled: Boolean): PercentageChartView {
        renderer!!.setDrawBackgroundEnabled(enabled)
        return this
    }

    /**
     * Sets the circular background color for this view.
     *
     * @param color the color of the circular background
     */
    fun backgroundColor(@ColorInt color: Int): PercentageChartView {
        renderer!!.setBackgroundColor(color)
        return this
    }

    /**
     * Sets the progress/progress bar color for this view.
     *
     * @param color the color of the progress/progress bar
     */
    fun progressColor(@ColorInt color: Int): PercentageChartView {
        renderer!!.setProgressColor(color)
        return this
    }

    /**
     * Sets progress gradient colors.
     *
     * @param type      The gradient type which is a GradientTypes constant
     * @param colors    The colors to be distributed.
     * There must be at least 2 colors in the array.
     * @param positions May be NULL. The relative position of
     * each corresponding color in the colors array, beginning
     * with 0 and ending with 1.0. If the values are not
     * monotonic, the drawing may produce unexpected results.
     * If positions is NULL, then the colors are automatically
     * spaced evenly.
     * @param angle     Defines the direction for linear gradient type.
     * @throws IllegalArgumentException If type is not a GradientTypes constant and if colors array is null
     */
    fun gradientColors(
        @GradientTypes type: Int,
        colors: IntArray?,
        positions: FloatArray?,
        @FloatRange(from = 0.0, to = 360.0) angle: Float
    ): PercentageChartView {
        require(!(type < BaseModeRenderer.GRADIENT_LINEAR || type > BaseModeRenderer.GRADIENT_SWEEP)) { "Invalid value for progress gradient type." }
        requireNotNull(colors) { "Gradient colors int array cannot be null." }
        positions?.let { renderer!!.setGradientColors(type, colors, it, angle) }
        return this
    }

    /**
     * Sets the duration of the progress change's animation.
     *
     * @param duration non-negative duration value.
     * @throws IllegalArgumentException if the given duration is less than 50.
     */
    fun animationDuration(@androidx.annotation.IntRange(from = 50) duration: Int): PercentageChartView {
        require(duration >= 50) { "Duration must be equal or greater than 50." }
        renderer!!.setAnimationDuration(duration)
        return this
    }

    /**
     * Sets the interpolator of the progress change's animation.
     *
     * @param interpolator TimeInterpolator instance.
     * @throws IllegalArgumentException if the given TimeInterpolator instance is null.
     */
    private fun animationInterpolator(interpolator: TimeInterpolator): PercentageChartView {
        renderer!!.setAnimationInterpolator(interpolator)
        return this
    }

    /**
     * Sets the text color for this view.
     *
     * @param color the text color
     */
    fun textColor(@ColorInt color: Int): PercentageChartView {
        renderer!!.setTextColor(color)
        return this
    }

    /**
     * Sets the text size.
     *
     * @param size the text size
     * @throws IllegalArgumentException if the given text size is zero or a negative value.
     */
    fun textSize(size: Float): PercentageChartView {
        require(size > 0) { "Text size must be a nonzero positive value." }
        renderer!!.setTextSize(size)
        return this
    }

    /**
     * Sets the text font.
     *
     * @param typeface the text font as a Typeface instance
     * @throws IllegalArgumentException if the given typeface is null.
     */
    fun typeface(typeface: Typeface): PercentageChartView {
        requireNotNull(typeface) { "Text TypeFace cannot be null" }
        renderer!!.setTypeface(typeface)
        return this
    }

    /**
     * Sets the text style.
     *
     * @param style the text style.
     * @throws IllegalArgumentException if the given text style is not a valid TextStyle constant.
     */
    fun textStyle(@TextStyle style: Int): PercentageChartView {
        require(!(style < 0 || style > 3)) { "Text style must be a valid TextStyle constant." }
        renderer!!.setTextStyle(style)
        return this
    }

    /**
     * Sets the text shadow. Passing zeros will remove the shadow.
     *
     * @param shadowColor  text shadow color value.
     * @param shadowRadius text shadow radius.
     * @param shadowDistX  text shadow y-axis distance.
     * @param shadowDistY  text shadow x-axis distance.
     */
    fun textShadow(
        @ColorInt shadowColor: Int,
        @FloatRange(from = 0.0) shadowRadius: Float,
        @FloatRange(from = 0.0) shadowDistX: Float,
        @FloatRange(from = 0.0) shadowDistY: Float
    ): PercentageChartView {
        renderer!!.setTextShadow(shadowColor, shadowRadius, shadowDistX, shadowDistY)
        return this
    }

    /**
     * Sets the offset of the circular background. Works only if chart mode is set to pie.
     *
     * @param offset A positive offset value.
     * @throws IllegalArgumentException if the given offset is a negative value, or, not supported by the current used chart mode.
     */
    fun backgroundOffset(@androidx.annotation.IntRange(from = 0) offset: Int): PercentageChartView {
        require(offset >= 0) { "Background offset must be a positive value." }
        try {
            (renderer as OffsetEnabledMode?)!!.backgroundOffset = offset
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("Background offset is not support by the used percentage chart mode.")
        }
        return this
    }

    /**
     * Sets whether background bar should be drawn.
     *
     * @param enabled True if background bar have to be drawn, false otherwise.
     * @throws IllegalArgumentException if background bar's drawing state is not supported by the current used chart mode.
     */
    fun drawBackgroundBarEnabled(enabled: Boolean): PercentageChartView {
        try {
            (renderer as RingModeRenderer?)!!.isDrawBackgroundBarEnabled = enabled
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("Background bar's drawing state is not support by the used percentage chart mode.")
        }
        return this
    }

    /**
     * Sets the background bar color.
     *
     * @param color the background bar color
     * @throws IllegalArgumentException if background bar color is not supported by the current used chart mode.
     */
    fun backgroundBarColor(@ColorInt color: Int): PercentageChartView {
        try {
            (renderer as RingModeRenderer?)!!.backgroundBarColor = color
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("Background bar color is not support by the used percentage chart mode.")
        }
        return this
    }

    /**
     * Sets the background bar thickness in pixels. Works only if chart mode is set to ring.
     *
     * @param thickness non-negative thickness value in pixels.
     * @throws IllegalArgumentException if the given value is negative, or, background bar thickness is not supported by the current used chart mode.
     */
    fun backgroundBarThickness(@FloatRange(from = 0.0) thickness: Float): PercentageChartView {
        require(thickness >= 0) { "Background bar thickness must be a positive value." }
        try {
            (renderer as RingModeRenderer?)!!.backgroundBarThickness = thickness
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("Background bar thickness is not support by the used percentage chart mode.")
        }
        return this
    }

    /**
     * Sets the progress bar thickness in pixels. Works only if chart mode is set to ring.
     *
     * @param thickness non-negative thickness value in pixels.
     * @throws IllegalArgumentException if the given value is negative, or, progress bar thickness is not supported by the current used chart mode.
     */
    fun progressBarThickness(@FloatRange(from = 0.0) thickness: Float): PercentageChartView {
        require(thickness >= 0) { "Progress bar thickness must be a positive value." }
        try {
            (renderer as RingModeRenderer?)!!.progressBarThickness = thickness
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("Progress bar thickness is not support by the used percentage chart mode.")
        }
        return this
    }

    /**
     * Sets the progress bar stroke style. Works only if chart mode is set to ring.
     *
     * @param style Progress bar stroke style as a ProgressStyle constant.
     * @throws IllegalArgumentException if the given progress bar style is not a valid ProgressBarStyle constant, or, not supported by the current used chart mode.
     */
    fun progressBarStyle(@ProgressBarStyle style: Int): PercentageChartView {
        require(!(style < 0 || style > 1)) { "Progress bar style must be a valid TextStyle constant." }
        try {
            (renderer as RingModeRenderer?)!!.progressBarStyle = style
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("Progress bar style is not support by the used percentage chart mode.")
        }
        return this
    }

    /**
     * Apply all the requested changes.
     */
    fun apply() {
        postInvalidate()
    }

    //##############################################################################################   ADAPTIVE COLOR PROVIDER
    fun setAdaptiveColorProvider(adaptiveColorProvider: AdaptiveColorProvider?) {
        renderer!!.setAdaptiveColorProvider(adaptiveColorProvider)
    }

    //##############################################################################################   TEXT FORMATTER
    fun setTextFormatter(textFormatter: ProgressTextFormatter?) {
        renderer!!.setTextFormatter(textFormatter)
    }

    //##############################################################################################   LISTENER
    fun setOnProgressChangeListener(onProgressChangeListener: OnProgressChangeListener?) {
        this.onProgressChangeListener = onProgressChangeListener
    }

    companion object {
        private const val STATE_SUPER_INSTANCE = "PercentageChartView.STATE_SUPER_INSTANCE"
        private const val STATE_MODE = "PercentageChartView.STATE_MODE"
        private const val STATE_ORIENTATION = "PercentageChartView.STATE_ORIENTATION"
        private const val STATE_START_ANGLE = "PercentageChartView.STATE_START_ANGLE"
        private const val STATE_DURATION = "PercentageChartView.STATE_DURATION"
        private const val STATE_PROGRESS = "PercentageChartView.STATE_PROGRESS"
        private const val STATE_PG_COLOR = "PercentageChartView.STATE_PG_COLOR"
        private const val STATE_DRAW_BG = "PercentageChartView.STATE_DRAW_BG"
        private const val STATE_BG_COLOR = "PercentageChartView.STATE_BG_COLOR"
        private const val STATE_BG_OFFSET = "PercentageChartView.STATE_BG_OFFSET"
        private const val STATE_TXT_COLOR = "PercentageChartView.STATE_TXT_COLOR"
        private const val STATE_TXT_SIZE = "PercentageChartView.STATE_TXT_SIZE"
        private const val STATE_TXT_SHA_COLOR = "PercentageChartView.STATE_TXT_SHD_COLOR"
        private const val STATE_TXT_SHA_RADIUS = "PercentageChartView.STATE_TXT_SHA_RADIUS"
        private const val STATE_TXT_SHA_DIST_X = "PercentageChartView.STATE_TXT_SHA_DIST_X"
        private const val STATE_TXT_SHA_DIST_Y = "PercentageChartView.STATE_TXT_SHA_DIST_Y"
        private const val STATE_PG_BAR_THICKNESS = "PercentageChartView.STATE_PG_BAR_THICKNESS"
        private const val STATE_PG_BAR_STYLE = "PercentageChartView.STATE_PG_BAR_STYLE"
        private const val STATE_DRAW_BG_BAR = "PercentageChartView.STATE_DRAW_BG_BAR"
        private const val STATE_BG_BAR_COLOR = "PercentageChartView.STATE_BG_BAR_COLOR"
        private const val STATE_BG_BAR_THICKNESS = "PercentageChartView.STATE_BG_BAR_THICKNESS"
        private const val STATE_GRADIENT_TYPE = "PercentageChartView.STATE_GRADIENT_TYPE"
        private const val STATE_GRADIENT_ANGLE = "PercentageChartView.STATE_GRADIENT_ANGLE"
        private const val STATE_GRADIENT_COLORS = "PercentageChartView.STATE_GRADIENT_COLORS"
        private const val STATE_GRADIENT_POSITIONS = "PercentageChartView.STATE_GRADIENT_POSITIONS"
    }
}