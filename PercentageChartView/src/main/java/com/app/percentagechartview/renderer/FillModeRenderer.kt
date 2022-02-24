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
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

class FillModeRenderer : BaseModeRenderer, OffsetEnabledMode {
    private var mDirectionAngle = 0f
    private var mBgSweepAngle = 0f
    private var mRadius = 0f

    constructor(view: IPercentageChartView?) : super(view!!) {
        setup()
    }

    constructor(view: IPercentageChartView?, attrs: TypedArray?) : super(view!!, attrs!!) {
        setup()
    }

    public override fun setup() {
        super.setup()
        mDirectionAngle = mStartAngle
    }

    override fun measure(
        w: Int,
        h: Int,
        paddingLeft: Int,
        paddingTop: Int,
        paddingRight: Int,
        paddingBottom: Int
    ) {
        val centerX = w / 2
        val centerY = h / 2
        mRadius = w.coerceAtMost(h).toFloat() / 2
        mCircleBounds?.set(centerX - mRadius, centerY - mRadius, centerX + mRadius,
            centerY + mRadius
        )
        measureBackgroundBounds()
        updateDrawingAngles()
        mCircleBounds?.let { setupGradientColors(it) }
        updateText()
    }

    private fun measureBackgroundBounds() {
        mBackgroundBounds?.let {
            it[mCircleBounds!!.left + mBackgroundOffset, mCircleBounds!!.top + mBackgroundOffset, mCircleBounds!!.right - mBackgroundOffset] =
                mCircleBounds!!.bottom - mBackgroundOffset
        }
    }

    override fun draw(canvas: Canvas) {
        //BACKGROUND
        if (mDrawBackground) {
            mBackgroundBounds?.let { mBackgroundPaint?.let { it1 ->
                canvas.drawArc(it, mStartAngle, mBgSweepAngle, false,
                    it1
                )
            } }
        }

        //FOREGROUND
        mCircleBounds?.let { mProgressPaint?.let { it1 ->
            canvas.drawArc(it, mStartAngle, mSweepAngle, false,
                it1
            )
        } }

        //TEXT
        canvas.let { drawText(it) }
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
        if (mGradientType == -1 || mGradientType == GRADIENT_SWEEP) return
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
        val height = mRadius - mProgress * (mRadius * 2) / DEFAULT_MAX
        val radiusPow = mRadius.toDouble().pow(2.0)
        val heightPow = height.toDouble().pow(2.0)
        mSweepAngle = if (height == 0f) 180F else Math.toDegrees(
            acos(
                (heightPow + radiusPow - sqrt(
                    radiusPow - heightPow
                ).pow(2.0)) / (2 * height * mRadius)
            )
        ).toFloat() * 2
        mStartAngle = mDirectionAngle - mSweepAngle / 2
        mBgSweepAngle = if (mBackgroundOffset > 0) 360F else mSweepAngle - 360
    }

    public override fun updateGradientAngle(angle: Float) {
        if (mGradientType == -1 || mGradientType == GRADIENT_RADIAL) return
        val matrix = Matrix()
        mCircleBounds?.let { matrix.postRotate(angle, it.centerX(), mCircleBounds!!.centerY()) }
        mGradientShader!!.setLocalMatrix(matrix)
    }

    override fun getStartAngle(): Float {
        return mDirectionAngle
    }

    override fun setStartAngle(angle: Float) {
        if (mDirectionAngle == angle) return
        mDirectionAngle = angle
        updateDrawingAngles()
    }

    //BACKGROUND OFFSET
    override var backgroundOffset: Int
        get() = mBackgroundOffset
        set(backgroundOffset) {
            if (!mDrawBackground || mBackgroundOffset == backgroundOffset) return
            mBackgroundOffset = backgroundOffset
            measureBackgroundBounds()
            updateDrawingAngles()
        }
}