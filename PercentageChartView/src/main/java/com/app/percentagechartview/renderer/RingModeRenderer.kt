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
package com.app.percentagechartview.renderer

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.Paint.Cap
import android.util.TypedValue
import com.app.percentagechartview.IPercentageChartView
import com.app.percentagechartview.R
import com.app.percentagechartview.callback.AdaptiveColorProvider

class RingModeRenderer : BaseModeRenderer, OrientationBasedMode {
    private var mBackgroundBarPaint: Paint? = null
    private var mDrawBackgroundBar = false
    private var mBackgroundBarThickness = 0f
    private var mBackgroundBarColor = 0
    private var mProvidedBgBarColor = 0
    private var mProgressBarStyle: Cap? = null
    private var mProgressBarThickness = 0f

    //TO PUSH PROGRESS BAR OUT OF SWEEP GRADIENT'S WAY
    private var tweakAngle = 0f

    constructor(view: IPercentageChartView) : super(view) {
        init()
        setup()
    }

    constructor(view: IPercentageChartView, attrs: TypedArray) : super(view, attrs) {
        init(attrs)
        setup()
    }

    private fun init(attrs: TypedArray) {
        //BACKGROUND BAR DRAW STATE
        mDrawBackgroundBar =
            attrs.getBoolean(R.styleable.PercentageChartView_pcv_drawBackgroundBar, true)

        //BACKGROUND WIDTH
        mBackgroundBarThickness = attrs.getDimensionPixelSize(
            R.styleable.PercentageChartView_pcv_backgroundBarThickness,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_BG_BAR_DP_WIDTH,
                mView!!.viewContext.resources.displayMetrics
            ).toInt()
        ).toFloat()

        //BACKGROUND BAR COLOR
        mBackgroundBarColor =
            attrs.getColor(R.styleable.PercentageChartView_pcv_backgroundBarColor, Color.BLACK)

        //PROGRESS WIDTH
        mProgressBarThickness = attrs.getDimensionPixelSize(
            R.styleable.PercentageChartView_pcv_progressBarThickness,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_PROGRESS_BAR_DP_WIDTH,
                mView!!.viewContext.resources.displayMetrics
            ).toInt()
        ).toFloat()

        //PROGRESS BAR STROKE STYLE
        val cap = attrs.getInt(R.styleable.PercentageChartView_pcv_progressBarStyle, CAP_ROUND)
        mProgressBarStyle = if (cap == CAP_ROUND) Cap.ROUND else Cap.BUTT
    }

    private fun init() {
        //DRAW BACKGROUND BAR
        mDrawBackgroundBar = true

        //BACKGROUND WIDTH
        mBackgroundBarThickness = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            DEFAULT_BG_BAR_DP_WIDTH,
            mView!!.viewContext.resources.displayMetrics
        )

        //BACKGROUND BAR COLOR
        mBackgroundBarColor = Color.BLACK

        //PROGRESS BAR WIDTH
        mProgressBarThickness = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            DEFAULT_PROGRESS_BAR_DP_WIDTH,
            mView!!.viewContext.resources.displayMetrics
        )

        //PROGRESS BAR STROKE STYLE
        mProgressBarStyle = Cap.ROUND
    }

    public override fun setup() {
        super.setup()
        mProvidedBgBarColor = -1
        tweakAngle = 0f
        updateDrawingAngles()

        //BACKGROUND BAR
        mBackgroundBarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBackgroundBarPaint!!.style = Paint.Style.STROKE
        mBackgroundBarPaint!!.color = mBackgroundBarColor
        mBackgroundBarPaint!!.strokeWidth = mBackgroundBarThickness
        mBackgroundBarPaint!!.strokeCap = mProgressBarStyle

        //PROGRESS PAINT
        mProgressPaint!!.style = Paint.Style.STROKE
        mProgressPaint!!.strokeWidth = mProgressBarThickness
        mProgressPaint!!.strokeCap = mProgressBarStyle
    }

    override fun measure(
        w: Int,
        h: Int,
        paddingLeft: Int,
        paddingTop: Int,
        paddingRight: Int,
        paddingBottom: Int
    ) {
        val diameter = Math.min(w, h)
        val maxOffset = Math.max(mProgressBarThickness, mBackgroundBarThickness)
        val centerX = w / 2
        val centerY = h / 2
        val radius = (diameter - maxOffset) / 2
        mCircleBounds!![centerX - radius, centerY - radius, centerX + radius] = centerY + radius
        val backgroundRadius = radius - mBackgroundBarThickness / 2 + 1
        mBackgroundBounds!![centerX - backgroundRadius, centerY - backgroundRadius, centerX + backgroundRadius] =
            centerY + backgroundRadius
        setupGradientColors(mCircleBounds!!)
        updateText()
    }

    override fun draw(canvas: Canvas) {
        //BACKGROUND
        if (mDrawBackground) {
            mBackgroundBounds?.let { mBackgroundPaint?.let { it1 ->
                canvas.drawArc(it, 0f, 360f, false,
                    it1
                )
            } }
        }

        //BACKGROUND BAR
        if (mDrawBackgroundBar) {
            if (mBackgroundBarThickness <= mProgressBarThickness) {
                mCircleBounds?.let {
                    canvas.drawArc(
                        it,
                        mStartAngle + tweakAngle,
                        -(360 - mSweepAngle + tweakAngle),
                        false,
                        mBackgroundBarPaint!!
                    )
                }
            } else {
                mCircleBounds?.let { canvas.drawArc(it, 0f, 360f, false, mBackgroundBarPaint!!) }
            }
        }

        //FOREGROUND
        if (mProgress != 0f) {
            mCircleBounds?.let {
                mProgressPaint?.let { it1 ->
                    canvas.drawArc(
                        it,
                        mStartAngle + tweakAngle,
                        mSweepAngle,
                        false,
                        it1
                    )
                }
            }
        }

        //TEXT
        drawText(canvas)
    }

    override fun destroy() {
        super.destroy()
        if (mBgBarColorAnimator != null) {
            if (mBgBarColorAnimator!!.isRunning) {
                mBgBarColorAnimator!!.cancel()
            }
            mBgBarColorAnimator!!.removeAllUpdateListeners()
        }
        mBgBarColorAnimator = null
        mBackgroundBarPaint = null
    }

    override fun setAdaptiveColorProvider(adaptiveColorProvider: AdaptiveColorProvider?) {
        if (adaptiveColorProvider == null) {
            mBgBarColorAnimator = null
            mTextColorAnimator = mBgBarColorAnimator
            mBackgroundColorAnimator = mTextColorAnimator!!
            mProgressColorAnimator = mBackgroundColorAnimator
            mAdaptiveColorProvider = null
            mTextPaint!!.color = mTextColor
            mBackgroundBarPaint!!.color = mBackgroundBarColor
            mBackgroundPaint!!.color = mBackgroundColor
            mProgressPaint!!.color = mProgressColor
            mView!!.postInvalidate()
            return
        }
        mAdaptiveColorProvider = adaptiveColorProvider
        setupColorAnimations()
        updateProvidedColors(mProgress)
        mView!!.postInvalidate()
    }

    public override fun setupGradientColors(bounds: RectF) {
        if (mGradientType == -1) return
        val ab = Math.pow((bounds.bottom - bounds.centerY()).toDouble(), 2.0)
        tweakAngle = Math.toDegrees(
            Math.acos(
                (2 * ab - Math.pow(
                    (mProgressBarThickness / 2).toDouble(),
                    2.0
                )) / (2 * ab)
            )
        ).toFloat()
        when (mGradientType) {
            GRADIENT_LINEAR -> {
                mGradientShader = LinearGradient(
                    bounds.centerX(),
                    bounds.top,
                    bounds.centerX(),
                    bounds.bottom,
                    mGradientColors,
                    mGradientDistributions,
                    Shader.TileMode.CLAMP
                )
                updateGradientAngle(mStartAngle)
            }
            GRADIENT_RADIAL -> mGradientShader = RadialGradient(
                bounds.centerX(),
                bounds.centerY(),
                bounds.bottom - bounds.centerY(),
                mGradientColors,
                mGradientDistributions,
                Shader.TileMode.MIRROR
            )
            GRADIENT_SWEEP -> {
                mGradientShader = SweepGradient(
                    bounds.centerX(),
                    bounds.centerY(),
                    mGradientColors,
                    mGradientDistributions
                )
                if (!mView!!.isInEditMode) {
                    // THIS BREAKS SWEEP GRADIENT'S PREVIEW MODE
                    updateGradientAngle(mStartAngle)
                }
            }
            else -> {
                mGradientShader = LinearGradient(
                    bounds.centerX(),
                    bounds.top,
                    bounds.centerX(),
                    bounds.bottom,
                    mGradientColors,
                    mGradientDistributions,
                    Shader.TileMode.CLAMP
                )
                updateGradientAngle(mStartAngle)
            }
        }
        mProgressPaint!!.shader = mGradientShader
    }

    public override fun setupColorAnimations() {
        super.setupColorAnimations()
        if (mBgBarColorAnimator == null) {
            mBgBarColorAnimator =
                ValueAnimator.ofObject(ArgbEvaluator(), mBackgroundBarColor, mProvidedBgBarColor)
            mBgBarColorAnimator!!.addUpdateListener { animation: ValueAnimator ->
                mProvidedBgBarColor = animation.animatedValue as Int
                mBackgroundBarPaint!!.color = mProvidedBgBarColor
            }
            mBgBarColorAnimator!!.duration = mAnimDuration.toLong()
        }
    }

    public override fun cancelAnimations() {
        super.cancelAnimations()
        if (mBgBarColorAnimator != null && mBgBarColorAnimator!!.isRunning) {
            mBgBarColorAnimator?.cancel()
        }
    }

    public override fun updateAnimations(progress: Float) {
        super.updateAnimations(progress)
        if (mAdaptiveColorProvider == null) return
        val providedBgBarColor = mAdaptiveColorProvider!!.provideBackgroundBarColor(progress)
        if (providedBgBarColor != -1 && providedBgBarColor != mProvidedBgBarColor) {
            val startColor =
                if (mProvidedBgBarColor != -1) mProvidedBgBarColor else mBackgroundBarColor
            mBgBarColorAnimator?.setIntValues(startColor, providedBgBarColor)
            mBgBarColorAnimator?.start()
        }
    }

    public override fun updateProvidedColors(progress: Float) {
        super.updateProvidedColors(progress)
        if (mAdaptiveColorProvider == null) return
        val providedBgBarColor = mAdaptiveColorProvider!!.provideBackgroundBarColor(progress)
        if (providedBgBarColor != -1 && providedBgBarColor != mProvidedBgBarColor) {
            mProvidedBgBarColor = providedBgBarColor
            mBackgroundBarPaint!!.color = mProvidedBgBarColor
        }
    }

    public override fun updateDrawingAngles() {
        mSweepAngle = when (orientation) {
            ORIENTATION_COUNTERCLOCKWISE -> -(mProgress / DEFAULT_MAX * 360)
            ORIENTATION_CLOCKWISE -> mProgress / DEFAULT_MAX * 360
            else -> mProgress / DEFAULT_MAX * 360
        }
    }

    public override fun updateGradientAngle(angle: Float) {
        if (mGradientType == -1 || mGradientType == GRADIENT_RADIAL) return
        val matrix = Matrix()
        matrix.postRotate(angle, mCircleBounds!!.centerX(), mCircleBounds!!.centerY())
        mGradientShader!!.setLocalMatrix(matrix)
    }

  /*  override var orientation: Int
        get() = orientation
        set(orientation) {
            if (this.orientation == orientation) return
            this.orientation = orientation
            updateDrawingAngles()
        }*/

    override fun setStartAngle(startAngle: Float) {
        if (mStartAngle == startAngle) return
        mStartAngle = startAngle
        if (mGradientType == GRADIENT_SWEEP) {
            updateGradientAngle(startAngle)
        }
    }

    // DRAW BACKGROUND BAR STATE
    var isDrawBackgroundBarEnabled: Boolean
        get() = mDrawBackgroundBar
        set(drawBackgroundBar) {
            if (mDrawBackgroundBar == drawBackgroundBar) return
            mDrawBackgroundBar = drawBackgroundBar
        }

    //BACKGROUND BAR COLOR
    var backgroundBarColor: Int
        get() = if (!mDrawBackgroundBar) -1 else mBackgroundBarColor
        set(backgroundBarColor) {
            if (!mDrawBackgroundBar || mAdaptiveColorProvider != null && mAdaptiveColorProvider!!.provideBackgroundBarColor(
                    mProgress
                ) != -1 || mBackgroundBarColor == backgroundBarColor
            ) return
            mBackgroundBarColor = backgroundBarColor
            mBackgroundBarPaint!!.color = mBackgroundBarColor
        }

    //BACKGROUND BAR THICKNESS
    var backgroundBarThickness: Float
        get() = mBackgroundBarThickness
        set(backgroundBarThickness) {
            if (mBackgroundBarThickness == backgroundBarThickness) return
            mBackgroundBarThickness = backgroundBarThickness
            mBackgroundBarPaint!!.strokeWidth = backgroundBarThickness
            measure(mView!!.width, mView!!.height, 0, 0, 0, 0)
        }

    //PROGRESS BAR THICKNESS
    var progressBarThickness: Float
        get() = mProgressBarThickness
        set(progressBarThickness) {
            if (mProgressBarThickness == progressBarThickness) return
            mProgressBarThickness = progressBarThickness
            mProgressPaint!!.strokeWidth = progressBarThickness
            measure(mView!!.width, mView!!.height, 0, 0, 0, 0)
        }

    //PROGRESS BAR STYLE
    var progressBarStyle: Int
        get() = if (mProgressBarStyle == Cap.ROUND) CAP_ROUND else CAP_SQUARE
        set(progressBarStyle) {
            require(!(progressBarStyle < 0 || progressBarStyle > 1)) { "Text style must be a valid TextStyle constant." }
            mProgressBarStyle = if (progressBarStyle == CAP_ROUND) Cap.ROUND else Cap.BUTT
            mProgressPaint!!.strokeCap = mProgressBarStyle
        }

    companion object {
        // BACKGROUND BAR
        private const val DEFAULT_BG_BAR_DP_WIDTH = 16f

        //PROGRESS BAR
        private const val DEFAULT_PROGRESS_BAR_DP_WIDTH = 16f
        const val CAP_ROUND = 0
        const val CAP_SQUARE = 1
    }
}