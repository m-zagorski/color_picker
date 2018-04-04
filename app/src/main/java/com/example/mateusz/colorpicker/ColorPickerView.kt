package com.example.mateusz.colorpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlin.math.roundToInt


class ColorPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

    private val chooseDrawable = ContextCompat.getDrawable(context, R.drawable.ic_choose_pallete1)
    private val colorPaint = Paint()
    private val scaleFactor = context.resources.displayMetrics.density
    private val maxVerticalRects = 7

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
        requestLayout()
        invalidate()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        val colorsPerRow = colors.size / 7 + if (colors.size % 7 == 0) 0 else 1
//        val colorWidth = MeasureSpec.getSize(widthMeasureSpec) / colorsPerRow.toFloat()
//        val colorHeight = colorWidth * 1.4F*scaleFactor
//        Log.e("MeasuringSize", "Size ${MeasureSpec.getSize(widthMeasureSpec)} AllColors " + colors.size + " PerWidth " + colorsPerRow + " ColorWidth " + colorWidth + " ColorHeight " + colorHeight
//                + " RoundedW: " + (Math.round(colorWidth*10F)/10F) + " RoundedH " + (Math.round(colorHeight*10F)/10F) + " SCALEFACTOR " + scaleFactor)
//        colorSize = ColorSize(colorWidth, colorHeight)
//        setMeasuredDimension(widthMeasureSpec,
//                View.resolveSize((maxVerticalRects * colorHeight).toInt(), heightMeasureSpec))

        setMeasuredDimension(widthMeasureSpec,
                View.resolveSize((maxVerticalRects * 7F * scaleFactor).toInt(), heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        var column = 0F
        colors.forEachIndexed { index, smallColor ->
            val indexModulo = index % maxVerticalRects
            if (indexModulo == 0 && index != 0) column++
            colorPaint.color = Color.parseColor(smallColor.hex)
            canvas.drawRect(column * colorSize.width, indexModulo * colorSize.height, column * colorSize.width + colorSize.width, indexModulo * colorSize.height + colorSize.height, colorPaint)
        }

        chooseDrawable?.apply {
            val currentX = currentPosition.x.toInt()
            val currentY = currentPosition.y.toInt()
            if (currentPosition.internal) decideVerticalColorPosition(currentX, currentY)
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
                        normalizeXCoordinate(Math.max(ev.x - (chooseDrawable?.let { it.intrinsicWidth / 2 }
                                ?: 0), 0F)),
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