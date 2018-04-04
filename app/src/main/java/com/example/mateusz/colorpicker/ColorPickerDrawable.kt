package com.example.mateusz.colorpicker

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.IntRange


class ColorPickerDrawable(private val context: Context,
                          private val colors: List<SmallColor>) : Drawable() {

    private val scaleFactor = context.resources.displayMetrics.density
    private val rectSize = 4F
    private val maxVerticalRects = 49

    private val size = rectSize * scaleFactor

    private val colorPaint = Paint().apply {
        color = Color.RED
    }

    override fun draw(canvas: Canvas) {
        var column = 0F
        colors.forEachIndexed { index, smallColor ->
            val indexModulo = index % maxVerticalRects
            if (indexModulo == 0 && index != 0) column++
            colorPaint.color = Color.parseColor(smallColor.hex)
            canvas.drawRect(column * size, indexModulo * size, column * size + size, indexModulo * size + size, colorPaint)
        }
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
//        needCalculatePath = true
    }

    override fun setAlpha(@IntRange(from = 0, to = 255) alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }
}