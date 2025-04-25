package com.sqq.adataylor.ui.remainder

import android.util.Log
import androidx.lifecycle.ViewModel
import com.sqq.adataylor.core.AdaTaylorCore
import com.sqq.adataylor.core.RemainderCore
import com.sqq.adataylor.core.RemainderType
import com.sqq.adataylor.data.FunctionModel
import kotlin.math.abs

class RemainderViewModel : ViewModel() {
    
    private val adaTaylorCore = AdaTaylorCore()
    private val remainderCore = RemainderCore()
    
    /**
     * 计算泰勒展开余项
     */
    fun calculateRemainder(
        function: FunctionModel,
        x: Double,
        x0: Double,
        order: Int,
        remainderType: RemainderType
    ): RemainderResult {
        // 计算精确值
        val exactValue = function.mainFunction(x)
        
        // 计算泰勒展开近似值
        val derivatives = function.derivativeFunctions
            .take(order + 1)
            .map { it(x0) }
        
        val approximateValue = adaTaylorCore.computeTaylorExpansion(x, x0, derivatives, order)
        
        // 计算实际误差
        val actualError = abs(exactValue - approximateValue)
        
        // 计算余项值
        val remainderValue = remainderCore.calculateRemainder(
            function = function,
            x = x,
            x0 = x0,
            order = order,
            remainderType = remainderType
        )
        
        return RemainderResult(
            x = x,
            x0 = x0,
            order = order,
            exactValue = exactValue,
            approximateValue = approximateValue,
            actualError = actualError,
            remainderValue = abs(remainderValue),
            remainderType = remainderType
        )
    }
    
    /**
     * 分析不同阶数的余项变化
     */
    fun analyzeRemainderByOrder(
        function: FunctionModel,
        x: Double,
        x0: Double,
        remainderType: RemainderType,
        maxOrder: Int = 8  // 默认修改为8阶，防止超出实际支持范围
    ): List<Pair<Int, Double>> {
        val results = mutableListOf<Pair<Int, Double>>()
        
        for (order in 1..maxOrder) {
            try {
                val remainderValue = remainderCore.calculateRemainder(
                    function = function,
                    x = x,
                    x0 = x0,
                    order = order,
                    remainderType = remainderType
                )
                
                results.add(Pair(order, abs(remainderValue)))
            } catch (e: Exception) {
                // 记录已经计算的阶数，不中断整个过程
                Log.d("RemainderViewModel", "计算${order}阶余项失败: ${e.message}")
                break
            }
        }
        
        return results
    }
    
    /**
     * 获取泰勒展开式的LaTeX表示
     */
    fun getTaylorExpansionLatex(function: FunctionModel, x0: Double, order: Int): String {
        // 获取导数值
        val derivatives = function.derivativeFunctions
            .take(order + 1)
            .map { it(x0) }
        
        return adaTaylorCore.generateTaylorExpansionLatex(x0, derivatives, order)
    }
    
    /**
     * 获取余项的LaTeX表示
     */
    fun getRemainderLatex(
        function: FunctionModel,
        x: Double,
        x0: Double,
        order: Int,
        remainderType: RemainderType
    ): String {
        return remainderCore.generateRemainderLatex(
            function, x, x0, order, remainderType
        )
    }
}