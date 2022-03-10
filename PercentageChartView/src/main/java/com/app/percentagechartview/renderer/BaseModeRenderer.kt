package com.app.percentagechartview.renderer

import android.animation.ArgbEvaluator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.DynamicLayout
import android.text.Editable
import android.text.Layout
import android.text.TextPaint
import android.util.TypedValue
import android.view.InflateException
import android.view.animation.*
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.app.percentagechartview.IPercentageChartView
import com.app.percentagechartview.R
import com.app.percentagechartview.annotation.ProgressOrientation
import com.app.percentagechartview.callback.AdaptiveColorProvider
import com.app.percentagechartview.callback.ProgressTextFormatter

abstract class BaseModeRenderer {
    companion object{
        // CHART MODE
        const val MODE_RING = 0
        const val MODE_PIE = 1
        const val MODE_FILL = 2

        // ORIENTATION
        const val INVALID_ORIENTATION = -1
        const val ORIENTATION_CLOCKWISE = 0
        const val ORIENTATION_COUNTERCLOCKWISE = 1

        // CHART MODE
        const val INVALID_GRADIENT = -1
        const val GRADIENT_LINEAR = 0
        const val GRADIENT_RADIAL = 1
        const val GRADIENT_SWEEP = 2

        // TEXT
        private const val DEFAULT_TEXT_SP_SIZE = 12f

        //ANIMATIONS
        private const val DEFAULT_ANIMATION_INTERPOLATOR = 0
        const val LINEAR = 0
        const val ACCELERATE = 1
        const val DECELERATE = 2
        const val ACCELERATE_DECELERATE = 3
        const val ANTICIPATE = 4
        const val OVERSHOOT = 5
        const val ANTICIPATE_OVERSHOOT = 6
        const val BOUNCE = 7
        const val FAST_OUT_LINEAR_IN = 8
        const val FAST_OUT_SLOW_IN = 9
        const val LINEAR_OUT_SLOW_IN = 10

        private const val DEFAULT_START_ANGLE = 0
        private const val DEFAULT_ANIMATION_DURATION = 400
        const val DEFAULT_MAX = 100f
    }

    //##############################################################################################
    // BACKGROUND
    var mDrawBackground = false
    var mBackgroundPaint: Paint? = null
    var mBackgroundColor = 0
    var mBackgroundOffset = 0

    private var mProvidedBackgroundColor = 0

    // PROGRESS
    var mProgressPaint: Paint? = null
    var mProgressColor = 0

    lateinit var mGradientColors: IntArray
    lateinit var mGradientDistributions: FloatArray
    var mGradientType = 0
    var mGradientAngle = 0f
    var mGradientShader: Shader? = null

    // TEXT
    var mTextPaint: TextPaint? = null
    var mTextColor = 0
    private var mProvidedTextColor = 0
    private var mTextProgress = 0
    private var mTextSize = 0f
    private var mTextStyle = 0
    private var mTypeface: Typeface? = null
    private var mTextShadowColor = 0
    private var mTextShadowRadius = 0f
    private var mTextShadowDistY = 0f
    private var mTextShadowDistX = 0f
    private var mTextEditor: Editable? = null
    private var mTextLayout: DynamicLayout? = null

    // COMMON
    var mBackgroundBounds: RectF? = null
    var mCircleBounds: RectF? = null
     var mProgressColorAnimator: ValueAnimator?=null
    var mBackgroundColorAnimator:ValueAnimator?=null
     var mTextColorAnimator:ValueAnimator? =null
    var mBgBarColorAnimator:ValueAnimator? = null
    private lateinit var mProgressAnimator: ValueAnimator
    private var mAnimInterpolator: Interpolator? = null
    var mAnimDuration = 0
    var mProgress = 0f
    var mStartAngle = 0f
    var mSweepAngle = 0f

    private var mProvidedProgressColor = 0

    @ProgressOrientation
    var orientation = 0
    var mAdaptiveColorProvider: AdaptiveColorProvider? = null
    private var mProvidedTextFormatter: ProgressTextFormatter? = null
    private  var defaultTextFormatter:ProgressTextFormatter? = null

    var mView: IPercentageChartView? = null

    constructor(view: IPercentageChartView){
        mView = view

        //DRAWING ORIENTATION

        //DRAWING ORIENTATION
        orientation = ORIENTATION_CLOCKWISE

        //START DRAWING ANGLE

        //START DRAWING ANGLE
        mStartAngle = DEFAULT_START_ANGLE.toFloat()

        //BACKGROUND DRAW STATE

        //BACKGROUND DRAW STATE
        mDrawBackground = this is PieModeRenderer

        //BACKGROUND COLOR

        //BACKGROUND COLOR
        mBackgroundColor = Color.BLACK

        //PROGRESS

        //PROGRESS
        mProgress = 0.also { mTextProgress = it }.toFloat()

        //PROGRESS COLOR

        //PROGRESS COLOR
        mProgressColor = Color.RED

        //GRADIENT COLORS

        //GRADIENT COLORS
        mGradientType = -1
        mGradientAngle = mStartAngle

        //PROGRESS ANIMATION DURATION

        //PROGRESS ANIMATION DURATION
        mAnimDuration = DEFAULT_ANIMATION_DURATION

        //PROGRESS ANIMATION INTERPOLATOR

        //PROGRESS ANIMATION INTERPOLATOR
        mAnimInterpolator = LinearInterpolator()

        //TEXT COLOR

        //TEXT COLOR
        mTextColor = Color.WHITE

        //TEXT SIZE

        //TEXT SIZE
        mTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            DEFAULT_TEXT_SP_SIZE,
            mView!!.viewContext.resources.displayMetrics
        )

        //TEXT STYLE

        //TEXT STYLE
        mTextStyle = Typeface.NORMAL

        //TEXT SHADOW

        //TEXT SHADOW
        mTextShadowColor = Color.TRANSPARENT
        mTextShadowRadius = 0f
        mTextShadowDistX = 0f
        mTextShadowDistY = 0f

        //BACKGROUND OFFSET

        //BACKGROUND OFFSET
        mBackgroundOffset = 0
    }

    constructor(view: IPercentageChartView,typedArray: TypedArray){
        mView = view

        //DRAWING ORIENTATION

        //DRAWING ORIENTATION
        orientation = typedArray.getInt(
            R.styleable.PercentageChartView_pcv_orientation,
            ORIENTATION_CLOCKWISE
        )

        //START DRAWING ANGLE

        //START DRAWING ANGLE
        mStartAngle = typedArray.getInt(
            R.styleable.PercentageChartView_pcv_startAngle,
            DEFAULT_START_ANGLE
        ).toFloat()
        if (mStartAngle < 0 || mStartAngle > 360) {
            mStartAngle = DEFAULT_START_ANGLE.toFloat()
        }

        //BACKGROUND DRAW STATE

        //BACKGROUND DRAW STATE
        mDrawBackground = typedArray.getBoolean(
            R.styleable.PercentageChartView_pcv_drawBackground,
            this is PieModeRenderer || this is FillModeRenderer
        )

        //BACKGROUND COLOR

        //BACKGROUND COLOR
        mBackgroundColor =
            typedArray.getColor(R.styleable.PercentageChartView_pcv_backgroundColor, Color.BLACK)

        //PROGRESS

        //PROGRESS
        mProgress = typedArray.getFloat(R.styleable.PercentageChartView_pcv_progress, 0f)
        if (mProgress < 0) {
            mProgress = 0f
        } else if (mProgress > 100) {
            mProgress = 100f
        }
        mTextProgress = mProgress.toInt()

        //PROGRESS COLOR

        //PROGRESS COLOR
        mProgressColor =
            typedArray.getColor(R.styleable.PercentageChartView_pcv_progressColor, getThemeAccentColor())

        //GRADIENT COLORS

        //GRADIENT COLORS
        initGradientColors(typedArray)

        //PROGRESS ANIMATION DURATION

        //PROGRESS ANIMATION DURATION
        mAnimDuration = typedArray.getInt(
            R.styleable.PercentageChartView_pcv_animDuration,
            DEFAULT_ANIMATION_DURATION
        )

        //PROGRESS ANIMATION INTERPOLATOR

        //PROGRESS ANIMATION INTERPOLATOR
        val interpolator: Int = typedArray.getInt(
            R.styleable.PercentageChartView_pcv_animInterpolator,
            DEFAULT_ANIMATION_INTERPOLATOR
        )
        mAnimInterpolator = when (interpolator) {
            LINEAR -> LinearInterpolator()
            ACCELERATE -> AccelerateInterpolator()
            DECELERATE -> DecelerateInterpolator()
            ACCELERATE_DECELERATE -> AccelerateDecelerateInterpolator()
            ANTICIPATE -> AnticipateInterpolator()
            OVERSHOOT -> OvershootInterpolator()
            ANTICIPATE_OVERSHOOT -> AnticipateOvershootInterpolator()
            BOUNCE -> BounceInterpolator()
            FAST_OUT_LINEAR_IN -> FastOutLinearInInterpolator()
            FAST_OUT_SLOW_IN -> FastOutSlowInInterpolator()
            LINEAR_OUT_SLOW_IN -> LinearOutSlowInInterpolator()
            else -> LinearInterpolator()
        }

        //TEXT COLOR

        //TEXT COLOR
        mTextColor = typedArray.getColor(R.styleable.PercentageChartView_pcv_textColor, Color.WHITE)

        //TEXT SIZE

        //TEXT SIZE
        mTextSize = typedArray.getDimensionPixelSize(
            R.styleable.PercentageChartView_pcv_textSize,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                DEFAULT_TEXT_SP_SIZE,
                mView!!.viewContext.resources.displayMetrics
            ).toInt()
        ).toFloat()

        //TEXT TYPEFACE

        //TEXT TYPEFACE
        val typeface: String? = typedArray.getString(R.styleable.PercentageChartView_pcv_typeface)
        if (typeface != null && typeface.isNotEmpty()) {
            mTypeface = Typeface.createFromAsset(mView!!.viewContext.resources.assets, typeface)
        }

        //TEXT STYLE

        //TEXT STYLE
        mTextStyle = typedArray.getInt(R.styleable.PercentageChartView_pcv_textStyle, Typeface.NORMAL)
        if (mTextStyle > 0) {
            mTypeface =
                if (mTypeface == null) Typeface.defaultFromStyle(mTextStyle) else Typeface.create(
                    mTypeface,
                    mTextStyle
                )
        }

        //TEXT SHADOW

        //TEXT SHADOW
        mTextShadowColor =
            typedArray.getColor(R.styleable.PercentageChartView_pcv_textShadowColor, Color.TRANSPARENT)
        if (mTextShadowColor != Color.TRANSPARENT) {
            mTextShadowRadius =
                typedArray.getFloat(R.styleable.PercentageChartView_pcv_textShadowRadius, 0f)
            mTextShadowDistX =
                typedArray.getFloat(R.styleable.PercentageChartView_pcv_textShadowDistX, 0f)
            mTextShadowDistY =
                typedArray.getFloat(R.styleable.PercentageChartView_pcv_textShadowDistY, 0f)
        }

        //BACKGROUND OFFSET

        //BACKGROUND OFFSET
        mBackgroundOffset = typedArray.getDimensionPixelSize(
            R.styleable.PercentageChartView_pcv_backgroundOffset,
            0
        )
    }

    private fun initGradientColors(attrs: TypedArray) {
        //PROGRESS GRADIENT TYPE
        mGradientType = attrs.getInt(R.styleable.PercentageChartView_pcv_gradientType, -1)
        if (mGradientType == -1) return

        //ANGLE FOR LINEAR GRADIENT
        mGradientAngle = attrs.getInt(
            R.styleable.PercentageChartView_pcv_gradientAngle,
            mStartAngle.toInt()
        ).toFloat()

        //PROGRESS GRADIENT COLORS
        val gradientColors = attrs.getString(R.styleable.PercentageChartView_pcv_gradientColors)
        if (gradientColors != null) {
            val colors = gradientColors.split(",").toTypedArray()
            mGradientColors = IntArray(colors.size)
            try {
                for (i in colors.indices) {
                    mGradientColors[i] = Color.parseColor(colors[i].trim { it <= ' ' })
                }
            } catch (e: Exception) {
                throw InflateException("pcv_gradientColors attribute contains invalid hex color values.")
            }
        }

        //PROGRESS GRADIENT COLORS'S DISTRIBUTIONS
        val gradientDist =
            attrs.getString(R.styleable.PercentageChartView_pcv_gradientDistributions)
        if (gradientDist != null) {
            val distributions = gradientDist.split(",").toTypedArray()
            mGradientDistributions = FloatArray(distributions.size)
            try {
                for (i in distributions.indices) {
                    mGradientDistributions[i] = distributions[i].trim { it <= ' ' }.toFloat()
                }
            } catch (e: Exception) {
                throw InflateException("pcv_gradientDistributions attribute contains invalid values.")
            }
        }
    }

    open fun setup() {
        mCircleBounds = RectF()
        mBackgroundBounds = RectF()
        mProvidedTextColor = -1
        mProvidedBackgroundColor = mProvidedTextColor
        mProvidedProgressColor = mProvidedBackgroundColor

        //BACKGROUND PAINT
        mBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBackgroundPaint!!.color = mBackgroundColor

        //PROGRESS PAINT
        mProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mProgressPaint!!.color = mProgressColor

        //TEXT PAINT
        mTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        mTextPaint!!.textAlign = Paint.Align.CENTER
        mTextPaint!!.textSize = mTextSize
        mTextPaint!!.color = mTextColor
        if (mTypeface != null) {
            mTextPaint!!.typeface = mTypeface
        }
        if (mTextShadowColor != Color.TRANSPARENT) {
            mTextPaint!!.setShadowLayer(
                mTextShadowRadius,
                mTextShadowDistX,
                mTextShadowDistY,
                mTextShadowColor
            )
        }

        //TEXT LAYOUT
        defaultTextFormatter=object : ProgressTextFormatter{
            override fun provideFormattedText(progress: Float): CharSequence {
                return "$progress%"
            }
        }

        mTextEditor = Editable.Factory.getInstance()
            .newEditable(defaultTextFormatter!!.provideFormattedText(mTextProgress.toFloat()))
        mTextLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DynamicLayout.Builder.obtain(mTextEditor!!, mTextPaint!!, Int.MAX_VALUE)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 0f)
                .setJustificationMode(LineBreaker.JUSTIFICATION_MODE_NONE)
                .setBreakStrategy(LineBreaker.BREAK_STRATEGY_SIMPLE)
                .setIncludePad(false)
                .build()
        } else {
            DynamicLayout(
                mTextEditor!!,
                mTextPaint!!, Int.MAX_VALUE,
                Layout.Alignment.ALIGN_NORMAL,
                0F, 0F,
                false
            )
        }

        //ANIMATIONS
        mProgressAnimator = ValueAnimator.ofFloat(0f, mProgress)
        mProgressAnimator.duration = mAnimDuration.toLong()
        mProgressAnimator.interpolator = mAnimInterpolator
        mProgressAnimator.addUpdateListener(AnimatorUpdateListener { valueAnimator: ValueAnimator ->
            mProgress = valueAnimator.animatedValue as Float
            if (mProgress > 0 && mProgress <= 100) {
                mTextProgress = mProgress.toInt()
            } else if (mProgress > 100) {
                mTextProgress = 100
                mProgress = mTextProgress.toFloat()
            } else {
                mTextProgress = 0
                mProgress = mTextProgress.toFloat()
            }
            updateDrawingAngles()
            updateText()
            mView!!.onProgressUpdated(mProgress)
            mView!!.postInvalidateOnAnimation()
        })
    }

    open fun attach(view: IPercentageChartView) {
        mView = view
        setup()
    }

    //############################################################################################## INNER BEHAVIOR
    abstract fun measure(
        w: Int,
        h: Int,
        paddingLeft: Int,
        paddingTop: Int,
        paddingRight: Int,
        paddingBottom: Int
    )

    abstract fun draw(canvas: Canvas)

    open fun drawText(canvas: Canvas) {
        canvas.save()
        canvas.translate(
            mCircleBounds!!.centerX(),
            mCircleBounds!!.centerY() - (mTextLayout!!.height shr 1)
        )
        mTextLayout!!.draw(canvas)
        canvas.restore()
    }

    open fun destroy() {
        if (mProgressAnimator.isRunning) {
            mProgressAnimator.cancel()
        }
        mProgressAnimator.removeAllUpdateListeners()
        if (mProgressColorAnimator?.isRunning == true) {
            mProgressColorAnimator?.cancel()
        }
        mProgressColorAnimator?.removeAllUpdateListeners()
        if (mBackgroundColorAnimator != null) {
            if (mBackgroundColorAnimator?.isRunning == true) {
                mBackgroundColorAnimator?.cancel()
            }
            mBackgroundColorAnimator?.removeAllUpdateListeners()
        }
        if (mTextColorAnimator != null) {
            if (mTextColorAnimator?.isRunning == true) {
                mTextColorAnimator?.cancel()
            }
            mTextColorAnimator?.removeAllUpdateListeners()
        }
        mTextColorAnimator = null
        mBackgroundColorAnimator = mTextColorAnimator
        mProgressColorAnimator = mBackgroundColorAnimator
        mProgressColorAnimator?.let {
            mProgressAnimator = it
        }
        mBackgroundBounds = null
        mCircleBounds = mBackgroundBounds
        mTextPaint = null
        mProgressPaint = mTextPaint
        mBackgroundPaint = mProgressPaint
        mGradientShader = null
        mAdaptiveColorProvider = null
        mProvidedTextFormatter = null
        defaultTextFormatter = mProvidedTextFormatter
    }

    open fun updateText() {
        if (mTextEditor != null) {
            val text =
                mProvidedTextFormatter?.provideFormattedText(
                    mTextProgress.toFloat()
                )
                    ?: defaultTextFormatter!!.provideFormattedText(mTextProgress.toFloat())
            mTextEditor!!.clear()
            mTextEditor!!.append(text)
        }
    }

    abstract fun updateDrawingAngles()

    open fun updateProvidedColors(progress: Float) {
        if (mAdaptiveColorProvider == null) return
        val providedProgressColor = mAdaptiveColorProvider!!.provideProgressColor(progress)
        if (providedProgressColor != -1 && providedProgressColor != mProvidedProgressColor && mGradientType == -1) {
            mProvidedProgressColor = providedProgressColor
            mProgressPaint!!.color = mProvidedProgressColor
        }
        val providedBackgroundColor = mAdaptiveColorProvider!!.provideBackgroundColor(progress)
        if (providedBackgroundColor != -1 && providedBackgroundColor != mProvidedBackgroundColor) {
            mProvidedBackgroundColor = providedBackgroundColor
            mBackgroundPaint!!.color = mProvidedBackgroundColor
        }
        val providedTextColor = mAdaptiveColorProvider!!.provideTextColor(progress)
        if (providedTextColor != -1 && providedTextColor != mProvidedTextColor) {
            mProvidedTextColor = providedTextColor
            mTextPaint!!.color = mProvidedTextColor
        }
    }

    open fun setupColorAnimations() {
        if (mProgressColorAnimator == null) {
            mProgressColorAnimator =
                ValueAnimator.ofObject(ArgbEvaluator(), mProgressColor, mProvidedProgressColor)
            mProgressColorAnimator!!.addUpdateListener(AnimatorUpdateListener { animation: ValueAnimator ->
                mProvidedProgressColor = animation.animatedValue as Int
                mProgressPaint!!.color = mProvidedProgressColor
            })
            mProgressColorAnimator!!.duration = mAnimDuration.toLong()
        }
        if (mBackgroundColorAnimator == null) {
            mBackgroundColorAnimator =
                ValueAnimator.ofObject(ArgbEvaluator(), mBackgroundColor, mProvidedBackgroundColor)
            mBackgroundColorAnimator!!.addUpdateListener(AnimatorUpdateListener { animation: ValueAnimator ->
                mProvidedBackgroundColor = animation.animatedValue as Int
                mBackgroundPaint!!.color = mProvidedBackgroundColor
            })
            mBackgroundColorAnimator!!.setDuration(mAnimDuration.toLong())
        }
        if (mTextColorAnimator == null) {
            mTextColorAnimator =
                ValueAnimator.ofObject(ArgbEvaluator(), mTextColor, mProvidedTextColor)
            mTextColorAnimator?.addUpdateListener(AnimatorUpdateListener { animation: ValueAnimator ->
                mProvidedTextColor = animation.animatedValue as Int
                mTextPaint!!.color = mProvidedTextColor
            })
            mTextColorAnimator?.duration = mAnimDuration.toLong()
        }
    }

    open fun updateAnimations(progress: Float) {
        mProgressAnimator.setFloatValues(mProgress, progress)
        mProgressAnimator.start()
        if (mAdaptiveColorProvider == null) return
        val providedProgressColor = mAdaptiveColorProvider!!.provideProgressColor(progress)
        if (providedProgressColor != -1 && providedProgressColor != mProvidedProgressColor && mGradientType == -1) {
            mProvidedProgressColor = providedProgressColor
            mProgressPaint!!.color = mProvidedProgressColor
        }
        val providedBackgroundColor = mAdaptiveColorProvider!!.provideBackgroundColor(progress)
        if (providedBackgroundColor != -1 && providedBackgroundColor != mProvidedBackgroundColor) {
            mProvidedBackgroundColor = providedBackgroundColor
            mBackgroundPaint!!.color = mProvidedBackgroundColor
        }
        val providedTextColor = mAdaptiveColorProvider!!.provideTextColor(progress)
        if (providedTextColor != -1 && providedTextColor != mProvidedTextColor) {
            mProvidedTextColor = providedTextColor
            mTextPaint!!.color = mProvidedTextColor
        }
    }

    open fun cancelAnimations() {
        if (mProgressAnimator.isRunning) {
            mProgressAnimator.cancel()
        }
        if (mProgressColorAnimator?.isRunning == true) {
            mProgressColorAnimator?.cancel()
        }
        if (mBackgroundColorAnimator?.isRunning == true) {
            mBackgroundColorAnimator?.cancel()
        }
        if (mTextColorAnimator != null && mTextColorAnimator!!.isRunning) {
            mTextColorAnimator!!.cancel()
        }
    }

    abstract fun setupGradientColors(bounds: RectF)

    abstract fun updateGradientAngle(angle: Float)

    private fun getThemeAccentColor(): Int {
        val colorAttr: Int =
            android.R.attr.colorAccent
        val outValue = TypedValue()
        mView!!.viewContext.theme.resolveAttribute(colorAttr, outValue, true)
        return outValue.data
    }

    abstract fun setAdaptiveColorProvider(adaptiveColorProvider: AdaptiveColorProvider?)

    open fun setTextFormatter(textFormatter: ProgressTextFormatter?) {
        mProvidedTextFormatter = textFormatter
        updateText()
        mView!!.postInvalidate()
    }

    //PROGRESS
    open fun getProgress(): Float {
        return mProgress
    }

    open fun setProgress(progress: Float, animate: Boolean) {
        if (mProgress == progress) return
        cancelAnimations()
        if (!animate) {
            mProgress = progress
            mTextProgress = progress.toInt()
            updateProvidedColors(progress)
            updateDrawingAngles()
            updateText()
            mView!!.onProgressUpdated(mProgress)
            mView!!.postInvalidate()
            return
        }
        updateAnimations(progress)
    }

    open fun isDrawBackgroundEnabled(): Boolean {
        return mDrawBackground
    }

    open fun setDrawBackgroundEnabled(drawBackground: Boolean) {
        if (mDrawBackground == drawBackground) return
        mDrawBackground = drawBackground
    }

    //START ANGLE
    open fun getStartAngle(): Float {
        return mStartAngle
    }

    abstract fun setStartAngle(startAngle: Float)

    //BACKGROUND COLOR
    open fun getBackgroundColor(): Int {
        return if (!mDrawBackground) -1 else mBackgroundColor
    }

    open fun setBackgroundColor(backgroundColor: Int) {
        if (mAdaptiveColorProvider != null && mAdaptiveColorProvider!!.provideBackgroundColor(
                mProgress
            ) != -1 || mBackgroundColor == backgroundColor
        ) return
        mBackgroundColor = backgroundColor
        if (!mDrawBackground) return
        mBackgroundPaint!!.color = mBackgroundColor
    }

    //PROGRESS COLOR
    open fun getProgressColor(): Int {
        return mProgressColor
    }

    open fun setProgressColor(progressColor: Int) {
        if (mAdaptiveColorProvider != null && mAdaptiveColorProvider!!.provideProgressColor(
                mProgress
            ) != -1 || mProgressColor == progressColor
        ) return
        mProgressColor = progressColor
        mProgressPaint!!.color = progressColor
    }

    //GRADIENT COLORS
    open fun getGradientType(): Int {
        return mGradientType
    }

    open fun setGradientColors(type: Int, colors: IntArray, positions: FloatArray, angle: Float) {
        mGradientType = type
        mGradientColors = colors
        mGradientDistributions = positions
        mCircleBounds?.let { setupGradientColors(it) }
        if (mGradientType == GRADIENT_LINEAR && mGradientAngle != angle) {
            mGradientAngle = angle
            updateGradientAngle(mGradientAngle)
        }
    }

    open fun setGradientColorsInternal(
        type: Int,
        colors: IntArray,
        positions: FloatArray,
        angle: Float
    ) {
        mGradientType = type
        mGradientColors = colors
        mGradientDistributions = positions
        if (mGradientType == GRADIENT_LINEAR && mGradientAngle != angle) {
            mGradientAngle = angle
        }
    }

    open fun getGradientAngle(): Float {
        return mGradientAngle
    }

    open fun getGradientColors(): IntArray? {
        return mGradientColors
    }

    open fun getGradientDistributions(): FloatArray? {
        return mGradientDistributions
    }

    //ANIMATION DURATION
    open fun getAnimationDuration(): Int {
        return mAnimDuration
    }

    open fun setAnimationDuration(duration: Int) {
        if (mAnimDuration == duration) return
        mAnimDuration = duration
        mProgressAnimator.duration = mAnimDuration.toLong()
        mProgressColorAnimator!!.duration = mAnimDuration.toLong()
        mBackgroundColorAnimator!!.duration = mAnimDuration.toLong()
        if (mTextColorAnimator != null) {
            mTextColorAnimator!!.duration = mAnimDuration.toLong()
        }
        if (mBgBarColorAnimator != null) {
            mBgBarColorAnimator!!.duration = mAnimDuration.toLong()
        }
    }

    //ANIMATION INTERPOLATOR
    open fun getAnimationInterpolator(): TimeInterpolator? {
        return mProgressAnimator.interpolator
    }

    open fun setAnimationInterpolator(interpolator: TimeInterpolator?) {
        mProgressAnimator.interpolator = interpolator
    }

    //TEXT COLOR
    open fun getTextColor(): Int {
        return mTextColor
    }

    open fun setTextColor(@ColorInt textColor: Int) {
        if (mAdaptiveColorProvider != null && mAdaptiveColorProvider!!.provideTextColor(mProgress) != -1 || mTextColor == textColor) return
        mTextColor = textColor
        mTextPaint!!.color = textColor
    }

    //TEXT SIZE
    open fun getTextSize(): Float {
        return mTextSize
    }

    open fun setTextSize(textSize: Float) {
        if (mTextSize == textSize) return
        mTextSize = textSize
        mTextPaint!!.textSize = textSize
        updateText()
    }

    //TEXT TYPEFACE
    open fun getTypeface(): Typeface? {
        return mTypeface
    }

    open fun setTypeface(typeface: Typeface) {
        if (mTypeface != null && mTypeface == typeface) return
        mTypeface = if (mTextStyle > 0) Typeface.create(typeface, mTextStyle) else typeface
        mTextPaint!!.typeface = mTypeface
        updateText()
    }

    //TEXT STYLE
    open fun getTextStyle(): Int {
        return mTextStyle
    }

    open fun setTextStyle(mTextStyle: Int) {
        if (this.mTextStyle == mTextStyle) return
        this.mTextStyle = mTextStyle
        mTypeface =
            if (mTypeface == null) Typeface.defaultFromStyle(mTextStyle) else Typeface.create(
                mTypeface,
                mTextStyle
            )
        mTextPaint!!.typeface = mTypeface
        updateText()
    }

    //TEXT SHADOW
    open fun getTextShadowColor(): Int {
        return mTextShadowColor
    }

    open fun getTextShadowRadius(): Float {
        return mTextShadowRadius
    }

    open fun getTextShadowDistY(): Float {
        return mTextShadowDistY
    }

    open fun getTextShadowDistX(): Float {
        return mTextShadowDistX
    }

    open fun setTextShadow(
        shadowColor: Int,
        shadowRadius: Float,
        shadowDistX: Float,
        shadowDistY: Float
    ) {
        if (mTextShadowColor == shadowColor && mTextShadowRadius == shadowRadius && mTextShadowDistX == shadowDistX && mTextShadowDistY == shadowDistY) return
        mTextShadowColor = shadowColor
        mTextShadowRadius = shadowRadius
        mTextShadowDistX = shadowDistX
        mTextShadowDistY = shadowDistY
        mTextPaint!!.setShadowLayer(
            mTextShadowRadius,
            mTextShadowDistX,
            mTextShadowDistY,
            mTextShadowColor
        )
        updateText()
    }
}