package com.sqq.adataylor.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqq.adataylor.core.AdaTaylorCore
import com.sqq.adataylor.data.DataPoint
import com.sqq.adataylor.data.FunctionModel
import kotlin.math.exp
import kotlin.math.sin

class GalleryViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Taylor展开式可视化"
    }
    val text: LiveData<String> = _text
    
    private val adaTaylorCore = AdaTaylorCore()

    // 生成函数的数据点
    fun generateFunctionPoints(
        function: FunctionModel,
        start: Double,
        end: Double,
        x0: Double,
        order: Int,
        pointCount: Int = 100
    ): Pair<List<DataPoint>, List<DataPoint>> {
        val step = (end - start) / pointCount
        val exactPoints = mutableListOf<DataPoint>()
        val taylorPoints = mutableListOf<DataPoint>()
        
        // 生成导数值
        val derivatives = when(function.name) {
            "指数函数" -> List(order + 1) { exp(x0) }
            "正弦函数" -> listOf(sin(x0), kotlin.math.cos(x0), -sin(x0), -kotlin.math.cos(x0), sin(x0))
                .take(order + 1)
            else -> listOf(0.0)
        }
        
        for (i in 0..pointCount) {
            val x = start + i * step
            
            // 计算精确值
            val exactY = when(function.name) {
                "指数函数" -> exp(x)
                "正弦函数" -> sin(x)
                else -> 0.0
            }
            exactPoints.add(DataPoint(x, exactY))
            
            // 计算Taylor近似值
            val taylorY = adaTaylorCore.computeTaylorExpansion(x, x0, derivatives, order)
            taylorPoints.add(DataPoint(x, taylorY))
        }
        
        return Pair(exactPoints, taylorPoints)
    }
}