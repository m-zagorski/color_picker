package com.example.mateusz.colorpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlin.math.roundToInt


class ColorPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

    private val totalWidth by lazy {
        val displayMetrics = DisplayMetrics()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
    private val chooseDrawable = ContextCompat.getDrawable(context, R.drawable.ic_choose_pallete1)
    private val colorPaint = Paint()
    private val scaleFactor = context.resources.displayMetrics.density
    private val maxVerticalRects = 7
    private val totalHeight = 49 * scaleFactor

    private var colors: List<SmallColor> = listOf()
    private var currentPosition = SelectionPosition()
    private var colorSize = ColorSize(5F * scaleFactor, 7F * scaleFactor)


    private val verticalPositionSubject = PublishSubject.create<Int>()
    private val horizontalPositionSubject = PublishSubject.create<Int>()

    val verticalPositionObservable: Observable<Int> = verticalPositionSubject
    val horizontalPositionObservable: Observable<Int> = horizontalPositionSubject


    //
    fun moveHorizontally(position: Int) {
        currentPosition = currentPosition.copy(x = position * colorSize.width, internal = false)
        invalidate()
    }

    fun moveVertically(position: Int) {
        currentPosition = currentPosition.copy(y = position * colorSize.height, internal = false)
        invalidate()
    }

    fun setColor(colors: List<SmallColor>) {
        this.colors = colors

        val colorsPerRow = colors.size / maxVerticalRects + if (colors.size % maxVerticalRects == 0) 0 else 1
        colorSize = ColorSize(totalWidth / colorsPerRow.toFloat(),
                totalHeight / maxVerticalRects)

        requestLayout()
        invalidate()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec,
                View.resolveSize(totalHeight.toInt(), heightMeasureSpec))

//        setMeasuredDimension(widthMeasureSpec,
//                View.resolveSize((maxVerticalRects * 7F * scaleFactor).toInt(), heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        var column = 0F
        colors.forEachIndexed { index, smallColor ->
            val indexModulo = index % maxVerticalRects
            if (indexModulo == 0 && index != 0) column++
            colorPaint.color = Color.parseColor(smallColor.hex)
            canvas.drawRect(column * colorSize.width, indexModulo * colorSize.height, column * colorSize.width + colorSize.width, indexModulo * colorSize.height + colorSize.height, colorPaint)
        }

        val currentX = currentPosition.x.toInt()
        val currentY = currentPosition.y.toInt()
        chooseDrawable?.apply {
            if (currentPosition.internal) decideVerticalColorPosition(currentX.toInt(), currentY)
            setBounds(currentX, currentY, (currentX + 4 * colorSize.width).toInt(), (currentY + 4 * colorSize.height).toInt())
        }
        chooseDrawable?.draw(canvas)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP -> {
                currentPosition = SelectionPosition(
                        Math.min(measuredWidth - 3 * colorSize.width, normalizeXCoordinate(Math.max(ev.x - (chooseDrawable?.let { it.intrinsicWidth / 2 }
                                ?: 0), 0F))),
                        Math.min(measuredHeight - 3 * colorSize.height, normalizeYCoordinate(Math.max(ev.y - (chooseDrawable?.let { it.intrinsicHeight / 2 }
                                ?: 0), 0F))),
                        true)
            }
            else -> super.onTouchEvent(ev)
        }
        invalidate()
        return true
    }

    private fun decideVerticalColorPosition(x: Int, y: Int) {
        val roundToInt = (y / colorSize.height).roundToInt()
        val xPosition = (x / colorSize.width).roundToInt()
        verticalPositionSubject.onNext(roundToInt)
        horizontalPositionSubject.onNext(xPosition)
    }

    private fun normalizeXCoordinate(x: Float): Float {
        val modulo = x % colorSize.width
        return if (modulo == 0F) x else Math.max(x - modulo, 0F)
    }

    private fun normalizeYCoordinate(y: Float): Float {
        val modulo = y % colorSize.height == 0F
        return if (modulo) y else Math.max(y - y % colorSize.height, 0F)
    }

    data class ColorSize(val width: Float = 0F, val height: Float = 0F)
    data class SelectionPosition(val x: Float = 0F, val y: Float = 0F, val internal: Boolean = true)
}