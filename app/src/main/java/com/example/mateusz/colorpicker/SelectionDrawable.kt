package com.example.mateusz.colorpicker

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.IntRange


class SelectionDrawable(private val context: Context) : Drawable() {
    private val scaleFactor = context.resources.displayMetrics.density


    private val paint = Paint().apply{
        strokeWidth = 12f*scaleFactor
        color = Color.WHITE
        style =  Paint.Style.STROKE

    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(0F, 0F, 100F*scaleFactor, 100F*scaleFactor, paint)
    }


    override fun getOutline(outline: Outline?) {
        outline?.setRect(0,0, (100*scaleFactor).toInt(), (100*scaleFactor).toInt())
    }

    override fun setAlpha(@IntRange(from = 0, to = 255) alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }
}