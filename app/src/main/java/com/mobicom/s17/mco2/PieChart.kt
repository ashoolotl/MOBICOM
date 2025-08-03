package com.mobicom.s17.mco2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class PieChart(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var data: Map<String, Int> = emptyMap()
    private val paint = Paint()
    private val rectF = RectF()

    fun setData(data: Map<String, Int>) {
        this.data = data
        invalidate() // Redraw chart
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val total = data.values.sum()
        if (total == 0) return

        // Set the drawing area inside the view bounds
        val padding = 20f
        rectF.set(padding, padding, width - padding, height - padding)

        var startAngle = 0f
        val colors = listOf(Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.GREEN, Color.RED)
        var colorIndex = 0

        for ((_, value) in data) {
            val sweepAngle = (value.toFloat() / total) * 360f
            paint.color = colors[colorIndex % colors.size]
            paint.isAntiAlias = true
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)

            startAngle += sweepAngle
            colorIndex++
        }
    }
}
