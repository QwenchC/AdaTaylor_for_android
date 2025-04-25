package com.sqq.adataylor.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqq.adataylor.core.AdaTaylorCore
import com.sqq.adataylor.data.FunctionModel
import com.sqq.adataylor.data.PredefinedFunctions
import com.sqq.adataylor.data.TaylorResult
import kotlin.math.abs

class HomeViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "AdaTaylor: 自适应Taylor展开计算工具"
    }
    val text: LiveData<String> = _text
    
    private val adaTaylorCore = AdaTaylorCore()
    
    // 预定义函数列表
    val predefinedFunctions = PredefinedFunctions.getAllFunctions()
    
    // 计算Taylor展开
    fun calculateTaylor(function: FunctionModel, x: Double, x0: Double, order: Int): TaylorResult {
        // 获取0到order阶导数在x0的值
        val derivatives = function.derivativeFunctions.take(order + 1).map { it(x0) }
        
        val approximate = adaTaylorCore.computeTaylorExpansion(x, x0, derivatives, order)
        
        // 计算精确值
        val exact = function.mainFunction(x)
        
        // 估计误差（如果有下一阶导数）
        val errorEstimate = if (order + 1 < function.derivativeFunctions.size) {
            val nextDerivative = function.derivativeFunctions[order + 1](x0)
            adaTaylorCore.estimateError(x, x0, nextDerivative, order)
        } else {
            0.0
        }
        
        return TaylorResult(
            x = x,
            x0 = x0,
            exactValue = exact,
            approximateValue = approximate,
            error = abs(exact - approximate),
            order = order,
            errorEstimate = errorEstimate
        )
    }
    
    // 自适应计算Taylor展开
    fun calculateAdaptiveTaylor(function: FunctionModel, x: Double, x0: Double, targetError: Double): TaylorResult {
        val order = adaTaylorCore.adaptiveOrder(
            x, 
            x0, 
            function.derivativeFunctions, 
            targetError
        )
        
        return calculateTaylor(function, x, x0, order)
    }
}