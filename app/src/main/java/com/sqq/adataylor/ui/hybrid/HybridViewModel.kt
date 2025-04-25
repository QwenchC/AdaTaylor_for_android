package com.sqq.adataylor.ui.hybrid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqq.adataylor.core.WaveletTaylorCore
import com.sqq.adataylor.core.WaveletType
import com.sqq.adataylor.data.DataPoint
import com.sqq.adataylor.data.FunctionModel

class HybridViewModel : ViewModel() {
    
    private val _text = MutableLiveData<String>().apply {
        value = "小波-泰勒混合逼近"
    }
    val text: LiveData<String> = _text
    
    private val waveletTaylorCore = WaveletTaylorCore()
    
    /**
     * 执行混合逼近并生成可视化数据
     */
    fun performMixedApproximation(
        function: FunctionModel,
        intervals: List<Double>,
        taylorOrders: List<Int>,
        taylorExpansionPoints: List<Double>,
        waveletType: WaveletType,
        waveletLevels: Int,
        pointCount: Int = 200
    ): Triple<List<DataPoint>, List<DataPoint>, Double> {
        // 确定计算范围
        val start = intervals.first()
        val end = intervals.last()
        
        // 生成等间距的计算点
        val xValues = DoubleArray(pointCount) { i ->
            start + (end - start) * i / (pointCount - 1)
        }
        
        // 执行混合逼近
        val result = waveletTaylorCore.performMixedApproximation(
            function = function.mainFunction,
            xValues = xValues,
            intervals = intervals,
            taylorOrders = taylorOrders,
            taylorExpansionPoints = taylorExpansionPoints,
            waveletType = waveletType,
            waveletLevels = waveletLevels
        )
        
        // 转换为图表数据点
        val exactPoints = result.xValues.mapIndexed { index, x -> 
            DataPoint(x, result.exactValues[index]) 
        }
        
        val approximatePoints = result.xValues.mapIndexed { index, x ->
            DataPoint(x, result.approximateValues[index])
        }
        
        return Triple(exactPoints, approximatePoints, result.meanError)
    }
    
    /**
     * 比较不同方法的逼近误差
     */
    fun compareApproximationMethods(
        function: FunctionModel,
        x0: Double,
        taylorOrder: Int,
        waveletType: WaveletType,
        waveletLevels: Int,
        mixedIntervals: List<Double>,
        mixedTaylorOrders: List<Int>,
        mixedTaylorPoints: List<Double>,
        pointCount: Int = 100
    ): Map<String, Double> {
        val domain = function.domain
        val start = domain.first
        val end = domain.second
        
        // 生成等间距的计算点
        val xValues = DoubleArray(pointCount) { i ->
            start + (end - start) * i / (pointCount - 1)
        }
        
        // 1. 纯泰勒展开
        var taylorErrorSum = 0.0
        var validTaylorPoints = 0
        
        for (x in xValues) {
            val exactValue = function.mainFunction(x)
            val derivativeValues = function.derivativeFunctions.take(taylorOrder + 1).map { it(x0) }
            
            val taylorValue = waveletTaylorCore.adaTaylorCore.computeTaylorExpansion(
                x, x0, derivativeValues, taylorOrder
            )
            
            val error = kotlin.math.abs(exactValue - taylorValue)
            if (!error.isNaN()) {
                taylorErrorSum += error
                validTaylorPoints++
            }
        }
        
        // 2. 纯小波逼近
        val signal = DoubleArray(findNearestPowerOfTwo(pointCount)) { i ->
            if (i < xValues.size) function.mainFunction(xValues[i]) else 0.0
        }
        
        val waveletCoefficients = waveletTaylorCore.waveletCore.discreteWaveletTransform(signal, waveletType)
        val waveletReconstructed = waveletTaylorCore.waveletCore.inverseWaveletTransform(waveletCoefficients, waveletType)
        
        var waveletErrorSum = 0.0
        var validWaveletPoints = 0
        
        for (i in xValues.indices) {
            if (i < waveletReconstructed.size) {
                val error = kotlin.math.abs(function.mainFunction(xValues[i]) - waveletReconstructed[i])
                if (!error.isNaN()) {
                    waveletErrorSum += error
                    validWaveletPoints++
                }
            }
        }
        
        // 3. 混合逼近
        val mixedResult = waveletTaylorCore.performMixedApproximation(
            function = function.mainFunction,
            xValues = xValues,
            intervals = mixedIntervals,
            taylorOrders = mixedTaylorOrders,
            taylorExpansionPoints = mixedTaylorPoints,
            waveletType = waveletType,
            waveletLevels = waveletLevels
        )
        
        return mapOf(
            "Taylor" to (taylorErrorSum / validTaylorPoints),
            "Wavelet" to (waveletErrorSum / validWaveletPoints),
            "Mixed" to mixedResult.meanError
        )
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
}