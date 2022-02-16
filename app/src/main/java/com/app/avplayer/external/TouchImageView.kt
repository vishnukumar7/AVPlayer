package com.app.avplayer.external

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View.OnTouchListener
import android.widget.ImageView
import com.app.avplayer.external.CustomExoAudioPlayer.Companion.TAG
import kotlin.math.abs
import kotlin.math.roundToInt


@SuppressLint("AppCompatCustomView")
class TouchImageView : ImageView {
    var matrices = Matrix()

    // We can be in one of these 3 states
  companion object{
        val NONE = 0
        val DRAG = 1
        val ZOOM = 2
  }

    var mode = NONE

    // Remember some things for zooming
    var last = PointF()
    var start = PointF()
    var minScale = 1f
    var maxScale = 3f
    lateinit var m: FloatArray

    var redundantXSpace = 0f
    var redundantYSpace: Float = 0f

    var width = 0f;
    var height: Float = 0f
    val CLICK = 3
    var saveScale = 1f
    var right = 0f
    var bottom: Float = 0f
    var origWidth: Float = 0f
    var origHeight: Float = 0f
    var bmWidth: Float = 0f
    var bmHeight: Float = 0f

    var mScaleDetector: ScaleGestureDetector? = null
lateinit var ctx: Context
    private lateinit var gestureDetector: GestureDetector

    constructor(context: Context) : super(context) {
init(context)
    }

    constructor(context: Context,attributeSet: AttributeSet) : super(context,attributeSet){
init(context)
    }

    fun init(context: Context) {
        gestureDetector = GestureDetector(context,DoubleTapGestureListener())
        super.setClickable(true)
        ctx = context
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        matrix.setTranslate(1f, 1f)
        m = FloatArray(9)
        imageMatrix = matrix
        scaleType = ScaleType.MATRIX
        setOnTouchListener(OnTouchListener { v, event ->
            if (gestureDetector.onTouchEvent(event)) {
                return@OnTouchListener true
            }
            mScaleDetector!!.onTouchEvent(event)
            matrix.getValues(m)
            val x = m[Matrix.MTRANS_X]
            val y = m[Matrix.MTRANS_Y]
            val curr = PointF(event.x, event.y)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    last[event.x] = event.y
                    start.set(last)
                    mode = DRAG
                }
                MotionEvent.ACTION_MOVE -> if (mode === DRAG) {
                    var deltaX = curr.x - last.x
                    var deltaY = curr.y - last.y
                    val scaleWidth = (origWidth * saveScale).roundToInt().toFloat()
                    val scaleHeight = (origHeight * saveScale).roundToInt().toFloat()
                    if (scaleWidth < width) {
                        deltaX = 0f
                        if (y + deltaY > 0) deltaY = -y else if (y + deltaY < -bottom) deltaY =
                            -(y + bottom)
                    } else if (scaleHeight < height) {
                        deltaY = 0f
                        if (x + deltaX > 0) deltaX = -x else if (x + deltaX < -right) deltaX =
                            -(x + right)
                    } else {
                        if (x + deltaX > 0) deltaX = -x else if (x + deltaX < -right) deltaX =
                            -(x + right)
                        if (y + deltaY > 0) deltaY = -y else if (y + deltaY < -bottom) deltaY =
                            -(y + bottom)
                    }
                    matrix.postTranslate(deltaX, deltaY)
                    last[curr.x] = curr.y
                }
                MotionEvent.ACTION_UP -> {
                    mode = NONE
                    val xDiff = abs(curr.x - start.x).toInt()
                    val yDiff = abs(curr.y - start.y).toInt()
                    if (xDiff < CLICK && yDiff < CLICK) performClick()
                }
                MotionEvent.ACTION_POINTER_UP -> mode = NONE
            }
            imageMatrix = matrix
            invalidate()
            true // indicate event was handled
        })
    }
    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            Log.i(
                TAG,
                detector.scaleFactor.toString() + " " + detector.focusX + " " + detector.focusY
            )
            var mScaleFactor =
                Math.max(.95f, detector.scaleFactor).toDouble().coerceAtMost(1.05)
                    .toFloat()
            val origScale: Float = saveScale
            saveScale *= mScaleFactor
            if (saveScale > maxScale) {
                saveScale = maxScale
                mScaleFactor = maxScale / origScale
            } else if (saveScale < minScale) {
                saveScale = minScale
                mScaleFactor = minScale / origScale
            }
            right = width * saveScale - width - 2 * redundantXSpace * saveScale
            bottom = height * saveScale - height - 2 * redundantYSpace * saveScale
            if (origWidth * saveScale <= width || origHeight * saveScale <= height) {
                matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2)
                if (mScaleFactor < 1) {
                    matrix.getValues(m)
                    val x: Float = m.get(Matrix.MTRANS_X)
                    val y: Float = m.get(Matrix.MTRANS_Y)
                    if (mScaleFactor < 1) {
                        if (Math.round(origWidth * saveScale) < width) {
                            if (y < -bottom) matrix.postTranslate(
                                0F,
                                -(y + bottom)
                            ) else if (y > 0) matrix.postTranslate(0F, -y)
                        } else {
                            if (x < -right) matrix.postTranslate(
                                -(x + right),
                                0F
                            ) else if (x > 0) matrix.postTranslate(-x, 0F)
                        }
                    }
                }
            } else {
                matrix.postScale(mScaleFactor, mScaleFactor, detector.focusX, detector.focusY)
                matrix.getValues(m)
                val x: Float = m.get(Matrix.MTRANS_X)
                val y: Float = m.get(Matrix.MTRANS_Y)
                if (mScaleFactor < 1) {
                    if (x < -right) matrix.postTranslate(
                        -(x + right),
                        0F
                    ) else if (x > 0) matrix.postTranslate(-x, 0F)
                    if (y < -bottom) matrix.postTranslate(
                        0F,
                        -(y + bottom)
                    ) else if (y > 0) matrix.postTranslate(0F, -y)
                }
            }
            return true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        height = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        // Fit to screen.
        val scale: Float
        val scaleX = width / bmWidth
        val scaleY = height / bmHeight
        scale = scaleX.coerceAtMost(scaleY)
        matrix.setScale(scale, scale)
        imageMatrix = matrix
        saveScale = 1f

        // Center the image
        redundantYSpace = height - scale * bmHeight
        redundantXSpace = width - scale * bmWidth
        redundantYSpace /= 2.toFloat()
        redundantXSpace /= 2.toFloat()
        matrix.postTranslate(redundantXSpace, redundantYSpace)
        origWidth = width - 2 * redundantXSpace
        origHeight = height - 2 * redundantYSpace
        right = width * saveScale - width - 2 * redundantXSpace * saveScale
        bottom = height * saveScale - height - 2 * redundantYSpace * saveScale
        imageMatrix = matrix
    }

    inner class DoubleTapGestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        // event when double tap occurs
        override fun onDoubleTap(e: MotionEvent): Boolean {
            val x = e.x
            val y = e.y
            Log.i(TAG, "Tapped at: ($x,$y)")
            if (isZoomed()) {
                zoomOut()
            } else {
                zoomIn()
            }
            return true
        }
    }

    fun isZoomed(): Boolean {
        return saveScale > minScale // this seems to work
    }

    fun zoomIn() {
        Log.i(TAG, "Zooming in")
        // TODO: no idea how to do this
    }

    fun zoomOut() {
        Log.i(TAG, "Zooming out")
        // TODO: no idea how to do this
    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        bmWidth = bm.width.toFloat()
        bmHeight = bm.height.toFloat()
    }

    fun setMaxZoom(x: Float) {
        maxScale = x
    }


}