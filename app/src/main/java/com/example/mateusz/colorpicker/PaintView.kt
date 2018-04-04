package com.example.mateusz.colorpicker

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager


class PaintView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

    private val totalWidth by lazy {
        val displayMetrics = DisplayMetrics()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = totalWidth / 3
        setMeasuredDimension(View.resolveSize(width, widthMeasureSpec),
                View.resolveSize((width * 0.8F).toInt(), heightMeasureSpec))
    }
}