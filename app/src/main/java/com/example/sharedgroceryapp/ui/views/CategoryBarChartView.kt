package com.example.sharedgroceryapp.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.sharedgroceryapp.data.local.Category
import com.google.android.material.color.MaterialColors

class CategoryBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density
    private var categoryCounts: Map<String, Int> = emptyMap()

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 11f * density
    }

    private val countPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 12f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    init {
        axisPaint.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutline, Color.GRAY)
        labelPaint.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface)
        countPaint.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface)
    }

    fun setData(counts: Map<String, Int>) {
        this.categoryCounts = counts
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        val paddingLeft = 20f * density
        val paddingRight = 20f * density
        val paddingTop = 30f * density
        val paddingBottom = 30f * density

        val chartWidth = w - paddingLeft - paddingRight
        val chartHeight = h - paddingTop - paddingBottom

        // Draw bottom axis
        val axisY = h - paddingBottom
        canvas.drawLine(paddingLeft, axisY, w - paddingRight, axisY, axisPaint)

        val categories = Category.values()
        val numBars = categories.size
        val barSpacing = chartWidth / (numBars * 2 + 1)
        val barWidth = barSpacing // Space out bars equally

        // Find max count to scale bars
        val maxCount = categories.map { categoryCounts[it.name] ?: 0 }.maxOrNull() ?: 1
        val scaleMax = if (maxCount == 0) 1 else maxCount

        for (i in categories.indices) {
            val category = categories[i]
            val count = categoryCounts[category.name] ?: 0

            // X-coordinates
            val left = paddingLeft + barSpacing + i * (barWidth + barSpacing)
            val right = left + barWidth

            // Y-coordinates scaled
            val barHeight = (count.toFloat() / scaleMax) * (chartHeight - 15f * density) // reserve space for text above
            val top = axisY - barHeight

            // Draw bar if count > 0
            if (count > 0) {
                barPaint.color = Color.parseColor(category.colorHex)
                // Draw rounded top bar
                val rect = RectF(left, top, right, axisY)
                val rx = 6f * density
                canvas.drawRoundRect(rect, rx, rx, barPaint)

                // Clean the bottom corners by drawing a flat bottom rect over it if needed
                // but standard roundrect is also fine
            }

            // Draw label
            val shortLabel = getShortLabel(category)
            canvas.drawText(shortLabel, (left + right) / 2f, axisY + 18f * density, labelPaint)

            // Draw count above bar
            val countY = if (count > 0) top - 6f * density else axisY - 6f * density
            canvas.drawText(count.toString(), (left + right) / 2f, countY, countPaint)
        }
    }

    private fun getShortLabel(category: Category): String {
        return when (category) {
            Category.FRUITS -> "Veg"
            Category.DAIRY -> "Dairy"
            Category.MEAT -> "Meat"
            Category.DRINKS -> "Drinks"
            Category.BAKERY -> "Bakery"
            Category.OTHER -> "Other"
        }
    }
}
