package com.sqq.adataylor.core

import com.sqq.adataylor.data.FunctionModel
import kotlin.math.abs
import kotlin.math.pow

/**
 * 泰勒展开余项计算核心类
 */
class RemainderCore {
    
    private val adaTaylorCore = AdaTaylorCore()
    
    /**
     * 计算泰勒展开的余项
     */
    fun calculateRemainder(
        function: FunctionModel,
        x: Double,
        x0: Double,
        order: Int,
        remainderType: RemainderType
    ): Double {
        return when (remainderType) {
            RemainderType.LAGRANGE -> calculateLagrangeRemainder(function, x, x0, order)
            RemainderType.CAUCHY -> calculateCauchyRemainder(function, x, x0, order)
            RemainderType.INTEGRAL -> calculateIntegralRemainder(function, x, x0, order)
        }
    }
    
    /**
     * 计算拉格朗日余项
     * R_n(x) = f^(n+1)(ξ)/(n+1)! * (x-x0)^(n+1)，其中ξ位于x0和x之间
     */
    private fun calculateLagrangeRemainder(
        function: FunctionModel,
        x: Double,
        x0: Double,
        order: Int
    ): Double {
        if (order + 1 >= function.derivativeFunctions.size) {
            throw IllegalArgumentException("函数的导数阶数不足，需要至少 ${order + 1} 阶导数")
        }
        
        // 估计ξ值，通常取x0和x之间的中点
        val xi = (x0 + x) / 2
        
        // 计算(n+1)阶导数值
        val derivative = function.derivativeFunctions[order + 1](xi)
        
        // 计算(n+1)!
        val factorial = adaTaylorCore.factorial(order + 1)
        
        // 计算(x-x0)^(n+1)
        val xMinusX0Pow = (x - x0).pow(order + 1)
        
        return derivative * xMinusX0Pow / factorial
    }
    
    /**
     * 计算柯西余项
     * R_n(x) = f^(n+1)(ξ)/n! * (x-x0)^n * (x-ξ)，其中ξ位于x0和x之间
     */
    private fun calculateCauchyRemainder(
        function: FunctionModel,
        x: Double,
        x0: Double,
        order: Int
    ): Double {
        if (order + 1 >= function.derivativeFunctions.size) {
            throw IllegalArgumentException("函数的导数阶数不足，需要至少 ${order + 1} 阶导数")
        }
        
        // 估计ξ值，取x0和x之间的一个点
        val xi = x0 + (x - x0) * 0.6 // 一个简单的估计，将ξ取在x0和x之间偏向x的位置
        
        // 计算(n+1)阶导数值
        val derivative = function.derivativeFunctions[order + 1](xi)
        
        // 计算n!
        val factorial = adaTaylorCore.factorial(order)
        
        // 计算(x-x0)^n
        val xMinusX0Pow = (x - x0).pow(order)
        
        // 计算(x-ξ)
        val xMinusXi = x - xi
        
        return derivative * xMinusX0Pow * xMinusXi / factorial
    }
    
    /**
     * 计算积分余项（这里使用数值积分近似）
     * R_n(x) = 1/n! * ∫(x0 to x) f^(n+1)(t) * (x-t)^n dt
     */
    private fun calculateIntegralRemainder(
        function: FunctionModel,
        x: Double,
        x0: Double,
        order: Int
    ): Double {
        if (order + 1 >= function.derivativeFunctions.size) {
            throw IllegalArgumentException("函数的导数阶数不足，需要至少 ${order + 1} 阶导数")
        }
        
        // 使用数值积分（梯形法则）计算
        val segments = 100
        val step = (x - x0) / segments
        var sum = 0.0
        
        for (i in 0 until segments) {
            val t1 = x0 + i * step
            val t2 = x0 + (i + 1) * step
            
            val value1 = integrandValue(function, x, t1, order)
            val value2 = integrandValue(function, x, t2, order)
            
            sum += (value1 + value2) * step / 2
        }
        
        return sum / adaTaylorCore.factorial(order)
    }
    
    /**
     * 计算积分被积函数的值 f^(n+1)(t) * (x-t)^n
     */
    private fun integrandValue(
        function: FunctionModel,
        x: Double,
        t: Double,
        order: Int
    ): Double {
        val derivativeValue = function.derivativeFunctions[order + 1](t)
        val factor = (x - t).pow(order)
        return derivativeValue * factor
    }
    
    /**
     * 生成余项的LaTeX表示
     */
    fun generateRemainderLatex(
        function: FunctionModel,
        x: Double,
        x0: Double,
        order: Int,
        remainderType: RemainderType
    ): String {
        val n = order
        
        return when (remainderType) {
            RemainderType.LAGRANGE -> {
                // R_n(x) = f^(n+1)(ξ)/(n+1)! * (x-x0)^(n+1)
                "R_{$n}(x) = \\frac{f^{(${n+1})}(\\xi)}{${n+1}!}(x-x_0)^{${n+1}}"
            }
            RemainderType.CAUCHY -> {
                // R_n(x) = f^(n+1)(ξ)/n! * (x-x0)^n * (x-ξ)
                "R_{$n}(x) = \\frac{f^{(${n+1})}(\\xi)}{${n}!}(x-x_0)^{${n}}(x-\\xi)"
            }
            RemainderType.INTEGRAL -> {
                // R_n(x) = 1/n! * ∫(x0 to x) f^(n+1)(t) * (x-t)^n dt
                "R_{$n}(x) = \\frac{1}{${n}!}\\int_{x_0}^{x}f^{(${n+1})}(t)(x-t)^{${n}}dt"
            }
        }
    }
}