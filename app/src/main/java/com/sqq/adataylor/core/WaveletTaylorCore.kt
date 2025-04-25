package com.sqq.adataylor.core

import kotlin.math.abs
import kotlin.math.pow

/**
 * 小波-泰勒混合逼近核心类
 * 结合小波分析与泰勒展开的优势进行函数逼近
 */
class WaveletTaylorCore {
    val adaTaylorCore = AdaTaylorCore()
    val waveletCore = WaveletCore()
    
    /**
     * 执行小波-泰勒混合逼近
     * @param function 目标函数
     * @param xValues 需要计算的x值数组
     * @param intervals 区间划分点列表 [x0, x1, x2, ..., xm]
     * @param taylorOrders 各区间泰勒展开阶数列表
     * @param taylorExpansionPoints 各区间泰勒展开点列表
     * @param waveletType 小波类型
     * @param waveletLevels 小波分解的最大尺度
     * @return 混合逼近结果
     */
    fun performMixedApproximation(
        function: (Double) -> Double,
        xValues: DoubleArray,
        intervals: List<Double>,
        taylorOrders: List<Int>,
        taylorExpansionPoints: List<Double>,
        waveletType: WaveletType,
        waveletLevels: Int
    ): MixedApproximationResult {
        if (intervals.size < 2 || taylorOrders.size != intervals.size - 1 || 
            taylorExpansionPoints.size != intervals.size - 1) {
            throw IllegalArgumentException("参数大小不匹配")
        }
        
        // 用于存储每个x点的近似值和误差
        val approximateValues = DoubleArray(xValues.size)
        val exactValues = DoubleArray(xValues.size)
        val errors = DoubleArray(xValues.size)
        
        // 对每个x点进行混合逼近
        for (i in xValues.indices) {
            val x = xValues[i]
            
            // 确定x所在区间
            val intervalIndex = findInterval(x, intervals)
            if (intervalIndex == -1) {
                // x点不在任何区间内
                approximateValues[i] = Double.NaN
                exactValues[i] = function(x)
                errors[i] = Double.NaN
                continue
            }
            
            // 计算区间内的精确函数值
            exactValues[i] = function(x)
            
            // 获取该区间的泰勒展开阶数和展开点
            val order = taylorOrders[intervalIndex]
            val x0 = taylorExpansionPoints[intervalIndex]
            
            // 1. 先进行泰勒展开计算
            val taylorValue = calculateTaylorPart(function, x, x0, order)
            
            // 2. 计算残差信号(精确值减去泰勒展开结果)
            val residual = exactValues[i] - taylorValue
            
            // 3. 对残差进行小波分析和重构
            val waveletValue = calculateWaveletPart(function, x, intervals[intervalIndex], 
                intervals[intervalIndex + 1], waveletType, waveletLevels, taylorValue)
            
            // 4. 最终混合近似值 = 泰勒展开 + 小波分析的残差修正
            approximateValues[i] = if (waveletValue.isNaN()) taylorValue else waveletValue
            
            // 计算误差
            errors[i] = abs(exactValues[i] - approximateValues[i])
        }
        
        return MixedApproximationResult(
            xValues = xValues,
            exactValues = exactValues,
            approximateValues = approximateValues,
            errors = errors,
            meanError = errors.filter { !it.isNaN() }.average()
        )
    }
    
    /**
     * 计算泰勒展开部分
     */
    private fun calculateTaylorPart(
        function: (Double) -> Double,
        x: Double,
        x0: Double,
        order: Int
    ): Double {
        // 计算0到order阶导数在x0的值
        val derivatives = calculateDerivatives(function, x0, order)
        
        // 使用泰勒展开计算近似值
        return adaTaylorCore.computeTaylorExpansion(x, x0, derivatives, order)
    }
    
    /**
     * 计算小波部分 - 使用残差进行小波分析和重构
     */
    private fun calculateWaveletPart(
        function: (Double) -> Double,
        x: Double,
        intervalStart: Double,
        intervalEnd: Double,
        waveletType: WaveletType,
        waveletLevels: Int,
        taylorValue: Double
    ): Double {
        // 对区间内一系列点进行采样，确保点数是2的幂次
        val sampleCount = findNearestPowerOfTwo(64)
        val sampledX = DoubleArray(sampleCount)
        val sampledResiduals = DoubleArray(sampleCount)
        
        // 生成等间距采样点
        val step = (intervalEnd - intervalStart) / (sampleCount - 1)
        for (i in 0 until sampleCount) {
            sampledX[i] = intervalStart + i * step
            val exactValue = function(sampledX[i])
            val taylorApprox = calculateTaylorPart(function, sampledX[i], (intervalStart + intervalEnd) / 2, 3)
            sampledResiduals[i] = exactValue - taylorApprox
        }
        
        try {
            // 对残差进行小波变换
            val coefficients = waveletCore.discreteWaveletTransform(sampledResiduals, waveletType)
            
            // 对系数进行处理
            
            // 小波重构
            val reconstructedResiduals = waveletCore.inverseWaveletTransform(coefficients, waveletType)
            
            // 为x点插值计算残差修正
            val waveletCorrection = interpolateValue(sampledX, reconstructedResiduals, x)
            
            // 返回修正后的值
            return taylorValue + waveletCorrection
        } catch (e: Exception) {
            // 如果小波处理失败，返回泰勒展开结果
            return taylorValue
        }
    }
    
    /**
     * 线性插值计算给定点的值
     */
    private fun interpolateValue(x: DoubleArray, y: DoubleArray, targetX: Double): Double {
        // 找到最靠近的两个点
        for (i in 0 until x.size - 1) {
            if (targetX >= x[i] && targetX <= x[i + 1]) {
                val ratio = (targetX - x[i]) / (x[i + 1] - x[i])
                return y[i] + ratio * (y[i + 1] - y[i])
            }
        }
        return Double.NaN
    }
    
    /**
     * 查找点所在的区间索引
     */
    private fun findInterval(x: Double, intervals: List<Double>): Int {
        for (i in 0 until intervals.size - 1) {
            if (x >= intervals[i] && x <= intervals[i + 1]) {
                return i
            }
        }
        return -1
    }
    
    /**
     * 数值微分计算导数
     */
    private fun calculateDerivatives(function: (Double) -> Double, x0: Double, maxOrder: Int): List<Double> {
        val derivatives = mutableListOf<Double>()
        val h = 0.0001 // 微分步长
        
        // 0阶导数就是函数值
        derivatives.add(function(x0))
        
        // 1阶导数(中心差分)
        if (maxOrder >= 1) {
            derivatives.add((function(x0 + h) - function(x0 - h)) / (2 * h))
        }
        
        // 2阶及更高阶导数(中心差分)
        for (order in 2..maxOrder) {
            val secondDeriv = (function(x0 + h) - 2 * function(x0) + function(x0 - h)) / (h * h)
            derivatives.add(secondDeriv)
        }
        
        return derivatives
    }
    
    /**
     * 找到最近的2的幂次
     */
    private fun findNearestPowerOfTwo(n: Int): Int {
        var power = 1
        while (power < n) {
            power *= 2
        }
        return power
    }
    
    /**
     * 估计混合逼近误差界
     */
    fun estimateErrorBound(
        functionMaxDerivative: Double,
        maxTaylorOrder: Int,
        intervalSize: Double,
        waveletLevel: Int
    ): Double {
        // 根据定理6.7计算误差上界
        val taylorError = functionMaxDerivative * intervalSize.pow(maxTaylorOrder + 1) / 
                          adaTaylorCore.factorial(maxTaylorOrder + 1)
        val waveletError = 0.5 * Math.pow(2.0, -waveletLevel.toDouble())
        
        return taylorError + waveletError
    }
}

/**
 * 混合逼近结果数据类
 */
data class MixedApproximationResult(
    val xValues: DoubleArray,
    val exactValues: DoubleArray,
    val approximateValues: DoubleArray,
    val errors: DoubleArray,
    val meanError: Double
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as MixedApproximationResult
        
        if (!xValues.contentEquals(other.xValues)) return false
        if (!exactValues.contentEquals(other.exactValues)) return false
        if (!approximateValues.contentEquals(other.approximateValues)) return false
        if (!errors.contentEquals(other.errors)) return false
        if (meanError != other.meanError) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = xValues.contentHashCode()
        result = 31 * result + exactValues.contentHashCode()
        result = 31 * result + approximateValues.contentHashCode()
        result = 31 * result + errors.contentHashCode()
        result = 31 * result + meanError.hashCode()
        return result
    }
}