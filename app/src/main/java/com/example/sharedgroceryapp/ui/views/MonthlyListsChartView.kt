package com.example.sharedgroceryapp.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.google.android.material.color.MaterialColors

class MonthlyListsChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density
    private var dataPoints: List<Pair<String, Int>> = emptyList()

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * density
        pathEffect = DashPathEffect(floatArrayOf(5f * density, 5f * density), 0f)
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f * density
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dotOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 10f * density
    }

    private val countPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 11f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private var primaryColor: Int = Color.GREEN
    private var secondaryColor: Int = Color.YELLOW
    private var surfaceColor: Int = Color.WHITE

    init {
        // Resolve theme colors
        primaryColor = MaterialColors.getColor(this, android.R.attr.colorPrimary)
        secondaryColor = MaterialColors.getColor(this, android.R.attr.colorSecondary)
        surfaceColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, Color.WHITE)

        gridPaint.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutline, Color.LTGRAY)
        axisPaint.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutline, Color.GRAY)
        
        linePaint.color = primaryColor
        dotPaint.color = secondaryColor
        dotOuterPaint.color = primaryColor

        textPaint.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface)
        countPaint.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface)
    }

    fun setData(points: List<Pair<String, Int>>) {
        this.dataPoints = points
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (dataPoints.isEmpty()) {
            return
        }

        val w = width.toFloat()
        val h = height.toFloat()

        val paddingLeft = 30f * density
        val paddingRight = 30f * density
        val paddingTop = 30f * density
        val paddingBottom = 35f * density

        val chartWidth = w - paddingLeft - paddingRight
        val chartHeight = h - paddingTop - paddingBottom
        val axisY = h - paddingBottom

        // Draw axes
        canvas.drawLine(paddingLeft, axisY, w - paddingRight, axisY, axisPaint)

        // Find max value to scale Y axis
        val maxVal = dataPoints.maxOf { it.second }
        val scaleMax = if (maxVal == 0) 4 else maxVal // default to 4 if all zero
        // Round max to multiple of 2 or 4 for nice grid lines
        val gridMax = if (scaleMax % 2 == 0) scaleMax else scaleMax + 1

        // Draw 3 horizontal grid lines (0%, 50%, 100% of height)
        for (gridIdx in 1..2) {
            val ratio = gridIdx.toFloat() / 2f
            val gridY = axisY - ratio * chartHeight
            canvas.drawLine(paddingLeft, gridY, w - paddingRight, gridY, gridPaint)
        }

        val numPoints = dataPoints.size
        val xSpacing = if (numPoints > 1) chartWidth / (numPoints - 1) else chartWidth

        // Build path for the line and path for the gradient fill
        val linePath = Path()
        val fillPath = Path()

        val pointsCoordinates = mutableListOf<PointF>()

        for (i in dataPoints.indices) {
            val count = dataPoints[i].second
            val x = paddingLeft + i * xSpacing
            val y = axisY - (count.toFloat() / gridMax) * chartHeight
            pointsCoordinates.add(PointF(x, y))

            if (i == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, axisY)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (i == dataPoints.size - 1) {
                fillPath.lineTo(x, axisY)
                fillPath.close()
            }
        }

        // Apply LinearGradient shader for premium area fill under the line
        val fillGrad = LinearGradient(
            0f, paddingTop,
            0f, axisY,
            adjustAlpha(primaryColor, 0.35f),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        fillPaint.shader = fillGrad

        // Draw filled area under the line
        canvas.drawPath(fillPath, fillPaint)

        // Draw the main line
        canvas.drawPath(linePath, linePaint)

        // Draw data points (dots) and labels
        for (i in dataPoints.indices) {
            val point = pointsCoordinates[i]
            val label = dataPoints[i].first
            val count = dataPoints[i].second

            // Draw month label under axis
            canvas.drawText(label, point.x, axisY + 18f * density, textPaint)

            // Draw count above dot
            canvas.drawText(count.toString(), point.x, point.y - 8f * density, countPaint)

            // Draw marker dot (outer border + inner fill)
            canvas.drawCircle(point.x, point.y, 6f * density, dotOuterPaint)
            dotPaint.color = surfaceColor
            canvas.drawCircle(point.x, point.y, 4f * density, dotPaint)
            dotPaint.color = primaryColor
            canvas.drawCircle(point.x, point.y, 3f * density, dotPaint)
        }
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }
}
