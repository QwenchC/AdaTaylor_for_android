package com.sqq.adataylor.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqq.adataylor.core.AdaTaylorCore
import com.sqq.adataylor.data.DataPoint
import com.sqq.adataylor.data.FunctionModel
import com.sqq.adataylor.data.FunctionManager
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.cos
import kotlin.math.sin

class GalleryViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Taylor展开式可视化"
    }
    val text: LiveData<String> = _text
    
    private val adaTaylorCore = AdaTaylorCore()

    // 生成函数的数据点 - 修复余弦和对数函数的计算
    fun generateFunctionPoints(
        function: FunctionModel,
        start: Double,
        end: Double,
        x0: Double,
        order: Int,
        pointCount: Int = 200
    ): Pair<List<DataPoint>, List<DataPoint>> {
        val step = (end - start) / pointCount
        val exactPoints = mutableListOf<DataPoint>()
        val taylorPoints = mutableListOf<DataPoint>()
        
        // 获取导数值 - 使用函数模型中的derivativeFunctions
        val derivatives = function.derivativeFunctions
            .take(order + 1)
            .map { it(x0) }
        
        for (i in 0..pointCount) {
            val x = start + i * step
            
            // 计算精确值 - 使用函数模型中的mainFunction
            val exactY = function.mainFunction(x)
            
            // 确保对数函数在定义域内
            if (function.name == "自然对数" && x <= 0) {
                continue // 跳过负数和零
            }
            
            exactPoints.add(DataPoint(x, exactY))
            
            // 计算Taylor近似值
            val taylorY = adaTaylorCore.computeTaylorExpansion(x, x0, derivatives, order)
            taylorPoints.add(DataPoint(x, taylorY))
        }
        
        return Pair(exactPoints, taylorPoints)
    }
    
    // 添加误差分析数据生成方法

    /**
     * 生成不同阶数的Taylor展开误差数据
     * @param function 函数模型
     * @param x 计算点
     * @param x0 展开点
     * @param maxOrder 最大阶数
     * @return 阶数-误差对应列表
     */
    fun generateErrorAnalysisData(
        function: FunctionModel,
        x: Double,
        x0: Double,
        maxOrder: Int = 10
    ): List<Pair<Int, Double>> {
        val result = mutableListOf<Pair<Int, Double>>()
        val exactValue = function.mainFunction(x)
        
        for (order in 0..maxOrder) {
            // 获取导数值
            val derivatives = function.derivativeFunctions
                .take(order + 1)
                .map { it(x0) }
            
            // 计算Taylor近似值
            val taylorValue = adaTaylorCore.computeTaylorExpansion(x, x0, derivatives, order)
            
            // 计算误差绝对值
            val error = kotlin.math.abs(exactValue - taylorValue)
            result.add(Pair(order, error))
        }
        
        return result
    }

    /**
     * 获取泰勒展开式文本表示
     */
    fun getTaylorExpansionText(function: FunctionModel, x0: Double, order: Int): String {
        // 获取导数值
        val derivatives = function.derivativeFunctions
            .take(order + 1)
            .map { it(x0) }
        
        return adaTaylorCore.generateTaylorExpansionText(x0, derivatives, order)
    }
}