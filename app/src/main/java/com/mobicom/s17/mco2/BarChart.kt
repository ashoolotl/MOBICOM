package com.mobicom.s17.mco2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BarChart(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var data: Map<String, Int> = emptyMap()
    private val paint = Paint()
    private val textPaint = Paint()

    fun setData(data: Map<String, Int>) {
        // Filter out moods with 0 values
        this.data = data.filter { it.value > 0 }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val barWidth = width / (data.size * 2f)
        val max = data.values.maxOrNull() ?: return

        textPaint.color = Color.BLACK
        textPaint.textSize = 36f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isAntiAlias = true

        val colors = mapOf(
            "Upset" to Color.GRAY,
            "Down" to Color.BLUE,
            "Neutral" to Color.GREEN,
            "Coping" to Color.rgb(255, 165, 0), // Orange
            "Elated" to Color.RED
        )

        var index = 0
        for ((mood, value) in data) {
            val left = index * 2 * barWidth + barWidth / 2
            val top = height - (value.toFloat() / max * (height * 0.6f)) - 100f
            val right = left + barWidth
            val bottom = height - 150f

            paint.color = colors[mood] ?: Color.BLACK
            canvas.drawRect(left, top, right, bottom, paint)

            // Draw value on top of bar
            canvas.drawText(value.toString(), left + barWidth / 2, top - 20f, textPaint)

            // Draw mood name below bar
            canvas.drawText(mood, left + barWidth / 2, height - 50f, textPaint)

            index++
        }
    }
}
