package com.sqq.adataylor.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.sqq.adataylor.data.DataPoint

/**
 * 自定义图表视图，用于绘制各种函数图像
 */
class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val exactFunctionPaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val approximationPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val thirdFunctionPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val axisPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val legendPaint = Paint().apply {
        color = Color.BLACK
        textSize = 30f
    }

    private var exactPoints: List<DataPoint> = emptyList()
    private var approximatePoints: List<DataPoint> = emptyList()
    private var thirdPoints: List<DataPoint> = emptyList()
    private var minX = 0.0
    private var maxX = 0.0
    private var minY = 0.0
    private var maxY = 0.0
    private var padding = 40f
    
    private var exactLabel = "精确值"
    private var approximateLabel = "近似值"
    private var thirdLabel = "第三曲线"
    private var drawLegend = true

    fun setData(
        exactPoints: List<DataPoint>,
        approximatePoints: List<DataPoint>,
        thirdPoints: List<DataPoint> = emptyList(),
        exactLabel: String = "精确值",
        approximateLabel: String = "近似值",
        thirdLabel: String = "第三曲线"
    ) {
        this.exactPoints = exactPoints
        this.approximatePoints = approximatePoints
        this.thirdPoints = thirdPoints
        this.exactLabel = exactLabel
        this.approximateLabel = approximateLabel
        this.thirdLabel = thirdLabel
        
        // 计算数据范围
        val allPoints = exactPoints + approximatePoints + thirdPoints
        if (allPoints.isNotEmpty()) {
            minX = allPoints.minOf { it.x }
            maxX = allPoints.maxOf { it.x }
            minY = allPoints.minOf { it.y }
            maxY = allPoints.maxOf { it.y }
            
            // 添加一些边距以便更好地显示
            val rangeX = maxX - minX
            val rangeY = maxY - minY
            minX -= rangeX * 0.1
            maxX += rangeX * 0.1
            minY -= rangeY * 0.1
            maxY += rangeY * 0.1
        }
        
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (exactPoints.isEmpty() && approximatePoints.isEmpty() && thirdPoints.isEmpty()) return
        
        val width = width.toFloat() - 2 * padding
        val height = height.toFloat() - 2 * padding
        
        // 绘制坐标轴
        canvas.drawLine(padding, height / 2 + padding, width + padding, height / 2 + padding, axisPaint) // X轴
        canvas.drawLine(padding, padding, padding, height + padding, axisPaint) // Y轴
        
        // 绘制原始函数曲线
        drawPointsList(canvas, exactPoints, exactFunctionPaint, width, height)
        
        // 绘制近似函数曲线
        drawPointsList(canvas, approximatePoints, approximationPaint, width, height)
        
        // 绘制第三条曲线(如果有)
        if (thirdPoints.isNotEmpty()) {
            drawPointsList(canvas, thirdPoints, thirdFunctionPaint, width, height)
        }
        
        // 绘制图例
        if (drawLegend) {
            val legendY = padding + 20
            
            // 精确函数图例
            canvas.drawLine(width - 200, legendY, width - 150, legendY, exactFunctionPaint)
            canvas.drawText(exactLabel, width - 140, legendY + 10, legendPaint)
            
            // 近似函数图例
            canvas.drawLine(width - 200, legendY + 40, width - 150, legendY + 40, approximationPaint)
            canvas.drawText(approximateLabel, width - 140, legendY + 50, legendPaint)
            
            // 第三条曲线图例(如果有)
            if (thirdPoints.isNotEmpty()) {
                canvas.drawLine(width - 200, legendY + 80, width - 150, legendY + 80, thirdFunctionPaint)
                canvas.drawText(thirdLabel, width - 140, legendY + 90, legendPaint)
            }
        }
    }
    
    private fun drawPointsList(canvas: Canvas, points: List<DataPoint>, paint: Paint, width: Float, height: Float) {
        if (points.isEmpty()) return
        
        val path = Path()
        val firstPoint = points.first()
        path.moveTo(
            mapXToCanvas(firstPoint.x, width),
            mapYToCanvas(firstPoint.y, height)
        )
        
        for (i in 1 until points.size) {
            val point = points[i]
            val canvasX = mapXToCanvas(point.x, width)
            val canvasY = mapYToCanvas(point.y, height)
            
            // 跳过NaN或无限值
            if (canvasX.isNaN() || canvasY.isNaN() || 
                canvasX.isInfinite() || canvasY.isInfinite()) {
                if (i < points.size - 1) {
                    val nextValidPoint = points[i + 1]
                    path.moveTo(
                        mapXToCanvas(nextValidPoint.x, width),
                        mapYToCanvas(nextValidPoint.y, height)
                    )
                }
                continue
            }
            
            path.lineTo(canvasX, canvasY)
        }
        
        canvas.drawPath(path, paint)
    }
    
    private fun mapXToCanvas(x: Double, width: Float): Float {
        return padding + ((x - minX) / (maxX - minX) * width).toFloat()
    }
    
    private fun mapYToCanvas(y: Double, height: Float): Float {
        return height + padding - ((y - minY) / (maxY - minY) * height).toFloat()
    }
    
    fun setShowLegend(show: Boolean) {
        drawLegend = show
        invalidate()
    }
}