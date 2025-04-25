package com.sqq.adataylor.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqq.adataylor.core.AdaTaylorCore
import com.sqq.adataylor.data.FunctionModel
import com.sqq.adataylor.data.TaylorResult
import kotlin.math.exp
import kotlin.math.sin

class HomeViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "AdaTaylor: 自适应Taylor展开计算工具"
    }
    val text: LiveData<String> = _text
    
    private val adaTaylorCore = AdaTaylorCore()
    
    // 预定义函数列表
    val predefinedFunctions = listOf(
        FunctionModel(
            "指数函数",
            "e^x",
            listOf("e^x", "e^x", "e^x", "e^x", "e^x")
        ),
        FunctionModel(
            "正弦函数",
            "sin(x)",
            listOf("sin(x)", "cos(x)", "-sin(x)", "-cos(x)", "sin(x)")
        )
    )
    
    // 计算Taylor展开
    fun calculateTaylor(function: FunctionModel, x: Double, x0: Double, order: Int): TaylorResult {
        // 这里简化实现，实际应该根据函数动态计算导数值
        val derivatives = when(function.name) {
            "指数函数" -> List(order + 1) { exp(x0) }
            "正弦函数" -> listOf(sin(x0), kotlin.math.cos(x0), -sin(x0), -kotlin.math.cos(x0), sin(x0))
                .take(order + 1)
            else -> listOf(0.0)
        }
        
        val approximate = adaTaylorCore.computeTaylorExpansion(x, x0, derivatives, order)
        
        // 计算精确值（实际应用中可能需要更复杂的方法）
        val exact = when(function.name) {
            "指数函数" -> exp(x)
            "正弦函数" -> sin(x)
            else -> 0.0
        }
        
        return TaylorResult(
            x = x,
            x0 = x0,
            exactValue = exact,
            approximateValue = approximate,
            error = kotlin.math.abs(exact - approximate),
            order = order
        )
    }
}