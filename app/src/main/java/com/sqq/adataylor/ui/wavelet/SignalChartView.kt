package com.sqq.adataylor.ui.wavelet

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * 自定义信号图表视图
 */
class SignalChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val signalPaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val axisPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private var signalData: DoubleArray = doubleArrayOf()
    private val padding = 20f

    /**
     * 设置信号数据
     */
    fun setSignalData(data: DoubleArray) {
        signalData = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat() - 2 * padding
        val height = height.toFloat() - 2 * padding
        
        // 绘制坐标轴
        canvas.drawLine(padding, height / 2 + padding, width + padding, height / 2 + padding, axisPaint) // X轴
        canvas.drawLine(padding, padding, padding, height + padding, axisPaint) // Y轴
        
        if (signalData.isEmpty()) return
        
        // 找到信号的最大和最小值，用于缩放
        var maxValue = Double.MIN_VALUE
        var minValue = Double.MAX_VALUE
        for (value in signalData) {
            maxValue = maxOf(maxValue, value)
            minValue = minOf(minValue, value)
        }
        
        // 确保有缩放空间
        val valueRange = maxOf(maxValue - minValue, 0.01)
        
        // 创建路径
        val path = Path()
        val stepX = width / (signalData.size - 1)
        
        for (i in signalData.indices) {
            val x = padding + i * stepX
            val normalizedValue = (signalData[i] - minValue) / valueRange
            val y = padding + height - (normalizedValue * height)
            
            if (i == 0) {
                path.moveTo(x, y.toFloat())
            } else {
                path.lineTo(x, y.toFloat())
            }
        }
        
        // 绘制信号路径
        canvas.drawPath(path, signalPaint)
    }
}