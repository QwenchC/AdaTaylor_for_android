package com.sqq.adataylor.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqq.adataylor.core.AdaTaylorCore
import com.sqq.adataylor.data.DataPoint
import com.sqq.adataylor.data.FunctionModel
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
}