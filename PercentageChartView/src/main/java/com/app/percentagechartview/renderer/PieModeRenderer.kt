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

import android.content.res.TypedArray
import android.graphics.*
import com.app.percentagechartview.IPercentageChartView
import com.app.percentagechartview.callback.AdaptiveColorProvider

class PieModeRenderer : BaseModeRenderer, OrientationBasedMode, OffsetEnabledMode {
    private var mBgStartAngle = 0f
    private var mBgSweepAngle = 0f

    constructor(view: IPercentageChartView) : super(view) {
        setup()
    }

    constructor(view: IPercentageChartView, attrs: TypedArray) : super(view, attrs) {
        setup()
    }

    public override fun setup() {
        super.setup()
        updateDrawingAngles()
    }

    override fun measure(
        w: Int,
        h: Int,
        paddingLeft: Int,
        paddingTop: Int,
        paddingRight: Int,
        paddingBottom: Int
    ) {
        val centerX = w * 0.5f
        val centerY = h * 0.5f
        val radius = Math.min(w, h) * 0.5f
        mCircleBounds!![centerX - radius, centerY - radius, centerX + radius] = centerY + radius
        measureBackgroundBounds()
        setupGradientColors(mCircleBounds!!)
        updateText()
    }

    private fun measureBackgroundBounds() {
        mBackgroundBounds!![mCircleBounds!!.left + mBackgroundOffset, mCircleBounds!!.top + mBackgroundOffset, mCircleBounds!!.right - mBackgroundOffset] =
            mCircleBounds!!.bottom - mBackgroundOffset
    }

    override fun draw(canvas: Canvas) {
        if (mGradientType == GRADIENT_SWEEP && mView!!.isInEditMode) {
            // TO GET THE RIGHT DRAWING START ANGLE FOR SWEEP GRADIENT'S COLORS IN PREVIEW MODE
            canvas.save()
            mCircleBounds?.let { canvas.rotate(mStartAngle, it.centerX(), mCircleBounds!!.centerY()) }
        }

        //FOREGROUND
        mCircleBounds?.let { canvas.drawArc(it, mStartAngle, mSweepAngle, true, mProgressPaint!!) }

        //BACKGROUND
        if (mDrawBackground) {
            mBackgroundBounds?.let { mBackgroundPaint?.let { it1 ->
                canvas.drawArc(it, mBgStartAngle, mBgSweepAngle, true,
                    it1
                )
            } }
        }
        if (mGradientType == GRADIENT_SWEEP && mView!!.isInEditMode) {
            // TO GET THE RIGHT DRAWING START ANGLE FOR SWEEP GRADIENT'S COLORS IN PREVIEW MODE
            canvas.restore()
        }

        //TEXT
        drawText(canvas)
    }

    override fun setAdaptiveColorProvider(adaptiveColorProvider: AdaptiveColorProvider?) {
        if (adaptiveColorProvider == null) {
            mTextColorAnimator = null
            mBackgroundColorAnimator = mTextColorAnimator!!
            mProgressColorAnimator = mBackgroundColorAnimator
            mAdaptiveColorProvider = null
            mTextPaint!!.color = mTextColor
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
        if (mGradientType == -1 && bounds.height() == 0f) return
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
                updateGradientAngle(mGradientAngle)
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
                updateGradientAngle(mGradientAngle)
            }
        }
        mProgressPaint!!.shader = mGradientShader
    }

    public override fun updateDrawingAngles() {
        when (orientation) {
            ORIENTATION_COUNTERCLOCKWISE -> {
                mSweepAngle = -(mProgress / DEFAULT_MAX * 360)
                mBgStartAngle = mStartAngle
                mBgSweepAngle = 360 + mSweepAngle
            }
            ORIENTATION_CLOCKWISE -> {
                mSweepAngle = mProgress / DEFAULT_MAX * 360
                mBgStartAngle = mStartAngle + mSweepAngle
                mBgSweepAngle = 360 - mSweepAngle
            }
            else -> {
                mSweepAngle = mProgress / DEFAULT_MAX * 360
                mBgStartAngle = mStartAngle + mSweepAngle
                mBgSweepAngle = 360 - mSweepAngle
            }
        }
    }

    public override fun updateGradientAngle(angle: Float) {
        if (mGradientType == -1 || mGradientType == GRADIENT_RADIAL) return
        val matrix = Matrix()
        mCircleBounds?.let { matrix.postRotate(angle, it.centerX(), mCircleBounds!!.centerY()) }
        mGradientShader!!.setLocalMatrix(matrix)
    }


   /* open fun getOrientation() : Int {
        orientation
    }

    fun setOrientation(value : Int){
        if(this.orientation==value)
            return
        this.orientation=value
        updateDrawingAngles()
    }*/
    /*override var orientation: Int
        get() = this.orientation
        set(orientation) {
            if (this.orientation == orientation) return
            this.orientation = orientation
            updateDrawingAngles()
        }*/

    override fun setStartAngle(startAngle: Float) {
        if (mStartAngle == startAngle) return
        mStartAngle = startAngle
        updateDrawingAngles()
        if (mGradientType == GRADIENT_SWEEP) {
            updateGradientAngle(startAngle)
        }
    }

    //BACKGROUND OFFSET
    override var backgroundOffset: Int
        get() = mBackgroundOffset
        set(backgroundOffset) {
            if (!mDrawBackground || mBackgroundOffset == backgroundOffset) return
            mBackgroundOffset = backgroundOffset
            measureBackgroundBounds()
        }
}