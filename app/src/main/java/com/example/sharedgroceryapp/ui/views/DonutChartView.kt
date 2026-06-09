package com.example.sharedgroceryapp.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.google.android.material.color.MaterialColors

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density
    private val strokeWidthPx = 18f * density

    private var activeCount: Int = 0
    private var completedCount: Int = 0

    private val completedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        strokeCap = Paint.Cap.ROUND
    }

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        strokeCap = Paint.Cap.ROUND
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val rectF = RectF()

    init {
        // Resolve theme colors dynamically for dark mode support
        completedPaint.color = MaterialColors.getColor(this, android.R.attr.colorPrimary)
        activePaint.color = MaterialColors.getColor(this, android.R.attr.colorSecondary)
        backgroundPaint.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutline, Color.LTGRAY)
        textPaint.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface)
        labelPaint.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY)
    }

    fun setData(active: Int, completed: Int) {
        this.activeCount = active
        this.completedCount = completed
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        val padding = strokeWidthPx / 2f + 10f * density
        val size = Math.min(width, height) - padding * 2f
        val left = (width - size) / 2f
        val top = (height - size) / 2f

        rectF.set(left, top, left + size, top + size)

        val total = activeCount + completedCount
        if (total == 0) {
            // Draw gray track representing empty state
            canvas.drawArc(rectF, 0f, 360f, false, backgroundPaint)
            
            // Draw 0%
            drawCenterText(canvas, "0%", context.getString(com.example.sharedgroceryapp.R.string.chart_no_items))
        } else {
            // Draw full background track first
            canvas.drawArc(rectF, 0f, 360f, false, backgroundPaint)

            val completedPercentage = completedCount.toFloat() / total
            val completedSweepAngle = completedPercentage * 360f
            val activeSweepAngle = 360f - completedSweepAngle

            // Draw Completed Segment
            if (completedSweepAngle > 0f) {
                canvas.drawArc(rectF, -90f, completedSweepAngle, false, completedPaint)
            }

            // Draw Active Segment
            if (activeSweepAngle > 0f) {
                canvas.drawArc(rectF, -90f + completedSweepAngle, activeSweepAngle, false, activePaint)
            }

            val percentText = "${(completedPercentage * 100).toInt()}%"
            drawCenterText(canvas, percentText, context.getString(com.example.sharedgroceryapp.R.string.chart_done_format, completedCount, total))
        }
    }

    private fun drawCenterText(canvas: Canvas, mainText: String, subText: String) {
        val cx = width / 2f
        val cy = height / 2f

        textPaint.textSize = 28f * density
        labelPaint.textSize = 12f * density

        val textHeight = textPaint.descent() - textPaint.ascent()
        val labelHeight = labelPaint.descent() - labelPaint.ascent()
        val spacing = 4f * density
        val totalHeight = textHeight + labelHeight + spacing

        val startY = cy - totalHeight / 2f - textPaint.ascent()

        canvas.drawText(mainText, cx, startY, textPaint)
        canvas.drawText(subText, cx, startY + textHeight + spacing - labelPaint.ascent() + textPaint.ascent(), labelPaint)
    }
}
