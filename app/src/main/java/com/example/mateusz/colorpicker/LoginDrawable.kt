package com.example.mateusz.colorpicker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import java.lang.Math.round
import java.util.*


class LoginDrawable @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

    private val scaleFactor = context.resources.displayMetrics.density
    private val dp by lazy {
        val displayMetrics = DisplayMetrics()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
        displayMetrics
    }

    private val elementWidth = round(dp.widthPixels / 7F).toFloat()
    private val pathEffect = CornerPathEffect(40F * scaleFactor)
    private val paint = Paint().apply {
        color = Color.WHITE
        pathEffect = pathEffect
    }

    // tmp 150 start height
    private val startHeight by lazy {
        140F * scaleFactor
//        dp.heightPixels / 7.5F
    }
    private val drainStep = 5

    private val random = Random()
    private var drains = AllDrains(listOf(), startHeight)


    init {
        post(RefreshProgressRunnable())
        drains = AllDrains(createDrains(), startHeight)
    }


    ///
    private val headerPath = Path()
    private val path = Path()


    private data class AllDrains(val drains: List<Drain> = listOf(), val startHeight: Float)
    private data class Drain(val totalHeight: Float, val currentHeight: Float = 0F, val done: Boolean = false)


    // 0 - 0
    // 1 - 1
    // 2 - 0
// 84 137
    private fun createDrains(): List<Drain> {
        var previousHeight = 0
        Log.e("StartHeight", "" + startHeight)
        return (0 until 7).map {
//            val height = if (it % 2 == 0) rand((60 * scaleFactor).toInt(), (140 * scaleFactor).toInt()) else rand(0, (70 * scaleFactor).toInt())
//            Log.e("GenerationErrors", "" + height + " SF " + scaleFactor)
            previousHeight = generateProperHeight(previousHeight, it % 2 == 0)
            Drain((previousHeight).toFloat())
        }
    }

    private tailrec fun generateProperHeigh1t(previousHeight: Int, long: Boolean): Int {
        val height: Int = if (long) rand((60 * scaleFactor).toInt(), (140 * scaleFactor).toInt()) else rand((35 * scaleFactor).toInt(), (70 * scaleFactor).toInt())
        return if (Math.abs(previousHeight - height) <= 30 * scaleFactor) generateProperHeigh1t(previousHeight, long) else height
    }

    private fun generateProperHeight(previousHeight: Int, long: Boolean): Int {
        val height: Int = if (long) rand((90 * scaleFactor).toInt(), (140 * scaleFactor).toInt()) else rand(0, (50 * scaleFactor).toInt())
        val difference = Math.abs(previousHeight - height)
        Log.e("MehMehMeh", "Difference $difference Previous $previousHeight Current $height")
        return height + if (difference <= 35 * scaleFactor) {
            val normalizedDifference = (35 * scaleFactor - difference).toInt()
            if (long) normalizedDifference else -normalizedDifference
        } else 0
    }

    override fun onDraw(canvas: Canvas) {
        path.reset()

        // Header
        headerPath.lineTo(0F, drains.startHeight)
        headerPath.lineTo(dp.widthPixels.toFloat(), drains.startHeight)
        headerPath.lineTo(dp.widthPixels.toFloat(), 0F)
        headerPath.lineTo(0F, 0F)

        path.lineTo(0 * elementWidth, drains.startHeight + drains.drains.first().currentHeight)

        (1..7).forEach {
            path.lineTo(it * elementWidth, drains.startHeight + drains.drains[it - 1].currentHeight)
            path.lineTo(it * elementWidth, drains.startHeight + if (it >= drains.drains.size) 0F else drains.drains[it].currentHeight)
        }

        ////

//        path.lineTo(0 * elementWidth, 400F)
//
//        path.lineTo(1 * elementWidth, 400F)
//        path.lineTo(1 * elementWidth, 200F)
//
//        path.lineTo(2 * elementWidth, 200f)
//        path.lineTo(2 * elementWidth, 800F)
//
//        path.lineTo(3 * elementWidth, 800F)
//        path.lineTo(3 * elementWidth, 500F)
//
//        path.lineTo(4 * elementWidth, 500F)
//        path.lineTo(4 * elementWidth, 700F)
//
//        path.lineTo(5 * elementWidth, 700F)
//        path.lineTo(5 * elementWidth, 550F)
//
//        path.lineTo(6 * elementWidth, 550F)
//        path.lineTo(6 * elementWidth, 900F)
//
//        path.lineTo(7 * elementWidth, 900F)
//        path.lineTo(7 * elementWidth, 0F)

        paint.pathEffect = null
        canvas.drawPath(headerPath, paint)
        paint.pathEffect = pathEffect
        canvas.drawPath(path, paint)
    }


    fun rand(from: Int, to: Int): Int {
        return random.nextInt(to - from) + from
    }

    private inner class RefreshProgressRunnable : Runnable {
        override fun run() {
            synchronized(this@LoginDrawable) {
                val start = System.currentTimeMillis()

                if (drains.drains.any { !it.done }) {
                    drains = AllDrains(drains.drains.map {
                        Log.e("Map", "TotalHeight ${it.totalHeight} CurrentHeight ${it.currentHeight} Done ${it.done}")
                        if (it.done) it else it.copy(currentHeight = it.currentHeight + determineDrainStep(it.currentHeight, drains.startHeight), done = it.totalHeight <= it.currentHeight)
                    }, startHeight)
                    invalidate()
                }


                val gap = 16 - (System.currentTimeMillis() - start)
//                postDelayed(this, 1)
                postDelayed(this, if (gap < 0) 0 else gap)
            }
        }

        private fun determineDrainStep(currentHeight: Float, startHeight: Float): Int {
            return if (currentHeight <= startHeight) 8 else rand(8, 20)
        }
    }
}