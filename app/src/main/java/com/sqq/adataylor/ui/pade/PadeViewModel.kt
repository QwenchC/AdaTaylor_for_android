package com.sqq.adataylor.ui.pade

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqq.adataylor.core.AdaTaylorCore
import com.sqq.adataylor.core.PadeCore
import com.sqq.adataylor.data.DataPoint
import com.sqq.adataylor.data.FunctionManager
import com.sqq.adataylor.data.FunctionModel
import kotlin.math.abs

class PadeViewModel : ViewModel() {
    
    private val _text = MutableLiveData<String>().apply {
        value = "泰勒-帕德比较"
    }
    val text: LiveData<String> = _text
    
    private val adaTaylorCore = AdaTaylorCore()
    private val padeCore = PadeCore()
    
    // 从函数管理器获取预定义函数列表
    val predefinedFunctions = FunctionManager.getAllFunctions()
    
    /**
     * 生成泰勒展开和帕德逼近的对比数据
     */
    fun generateComparisonData(
        function: FunctionModel,
        start: Double,
        end: Double,
        x0: Double,
        taylorOrder: Int,
        padeM: Int,
        padeN: Int,
        pointCount: Int = 200
    ): Triple<List<DataPoint>, List<DataPoint>, List<DataPoint>> {
        val step = (end - start) / pointCount
        
        // 计算每个点的精确值、泰勒展开值和帕德逼近值
        val exactPoints = mutableListOf<DataPoint>()
        val taylorPoints = mutableListOf<DataPoint>()
        val padePoints = mutableListOf<DataPoint>()
        
        // 获取导数函数
        val derivatives = function.derivativeFunctions
        
        // 获取Taylor系数（用于计算帕德逼近）
        val taylorCoefficients = mutableListOf<Double>()
        val maxOrder = taylorOrder.coerceAtLeast(padeM + padeN)
        
        for (i in 0..maxOrder) {
            if (i < derivatives.size) {
                taylorCoefficients.add(derivatives[i](x0) / adaTaylorCore.factorial(i))
            } else {
                taylorCoefficients.add(0.0)
            }
        }
        
        // 计算各点的值
        for (i in 0..pointCount) {
            val x = start + i * step
            
            // 精确值
            val exactValue = function.mainFunction(x)
            exactPoints.add(DataPoint(x, exactValue))
            
            // 泰勒展开值
            val taylorValue = adaTaylorCore.computeTaylorExpansion(
                x, 
                x0, 
                taylorCoefficients.take(taylorOrder + 1), 
                taylorOrder
            )
            taylorPoints.add(DataPoint(x, taylorValue))
            
            // 帕德逼近值
            try {
                val padeValue = padeCore.computePadeApproximation(
                    x, 
                    x0, 
                    taylorCoefficients, 
                    padeM, 
                    padeN
                )
                padePoints.add(DataPoint(x, padeValue))
            } catch (e: Exception) {
                // 如果帕德计算失败，使用NaN
                padePoints.add(DataPoint(x, Double.NaN))
            }
        }
        
        return Triple(exactPoints, taylorPoints, padePoints)
    }
    
    /**
     * 计算误差分析数据
     */
    fun calculateErrorAnalysis(
        function: FunctionModel,
        x0: Double,
        testPoints: List<Double>,
        taylorOrder: Int,
        padeM: Int,
        padeN: Int
    ): List<Triple<Double, Double, Double>> {
        val results = mutableListOf<Triple<Double, Double, Double>>()
        
        // 获取导数函数
        val derivatives = function.derivativeFunctions
        
        // 获取Taylor系数
        val taylorCoefficients = mutableListOf<Double>()
        val maxOrder = taylorOrder.coerceAtLeast(padeM + padeN)
        
        for (i in 0..maxOrder) {
            if (i < derivatives.size) {
                taylorCoefficients.add(derivatives[i](x0) / adaTaylorCore.factorial(i))
            } else {
                taylorCoefficients.add(0.0)
            }
        }
        
        // 对每个测试点计算误差
        for (x in testPoints) {
            // 计算精确值
            val exactValue = function.mainFunction(x)
            
            // 计算泰勒展开值
            val taylorValue = adaTaylorCore.computeTaylorExpansion(
                x, 
                x0, 
                taylorCoefficients.take(taylorOrder + 1), 
                taylorOrder
            )
            val taylorError = abs(exactValue - taylorValue)
            
            // 计算帕德逼近值
            val padeValue = try {
                padeCore.computePadeApproximation(
                    x, 
                    x0, 
                    taylorCoefficients, 
                    padeM, 
                    padeN
                )
            } catch (e: Exception) {
                Double.NaN
            }
            val padeError = if (padeValue.isNaN()) Double.NaN else abs(exactValue - padeValue)
            
            // 计算误差比率
            val errorRatio = if (padeError.isNaN() || padeError == 0.0) Double.NaN else taylorError / padeError
            
            results.add(Triple(abs(x - x0), taylorError, padeError))
        }
        
        return results
    }
    
    /**
     * 验证误差比定理
     * 检验 |f - Tn|_∞ / |f - R_m,n-m|_∞ = O(|x - x0|^m)
     */
    fun verifyErrorRatioTheorem(
        function: FunctionModel,
        x0: Double,
        points: List<Double>,
        taylorOrder: Int,
        padeM: Int,
        padeN: Int
    ): List<Pair<Double, Double>> {
        val results = mutableListOf<Pair<Double, Double>>()
        
        val errorData = calculateErrorAnalysis(function, x0, points, taylorOrder, padeM, padeN)
        
        for (data in errorData) {
            val distance = data.first // |x - x0|
            val taylorError = data.second
            val padeError = data.third
            
            if (!padeError.isNaN() && padeError > 0.0) {
                val ratio = taylorError / padeError
                val theoreticalRatio = Math.pow(distance, padeM.toDouble())
                
                results.add(Pair(ratio, theoreticalRatio))
            }
        }
        
        return results
    }
}