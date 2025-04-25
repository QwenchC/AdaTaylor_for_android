package com.sqq.adataylor.core

import kotlin.math.*

/**
 * AdaTaylor核心计算类
 * 实现Taylor展开式计算的核心功能
 */
class AdaTaylorCore {
    /**
     * 计算给定函数在给定点的Taylor展开式
     * @param x 展开点
     * @param x0 基准点
     * @param derivatives 在x0点的导数列表(0阶到n阶)
     * @param order 展开阶数
     * @return Taylor展开计算结果
     */
    fun computeTaylorExpansion(x: Double, x0: Double, derivatives: List<Double>, order: Int): Double {
        var result = 0.0
        for (n in 0..order) {
            if (n < derivatives.size) {
                result += derivatives[n] * (x - x0).pow(n) / factorial(n)
            }
        }
        return result
    }

    /**
     * 计算阶乘
     */
    fun factorial(n: Int): Double {
        var result = 1.0
        for (i in 2..n) {
            result *= i
        }
        return result
    }
    
    /**
     * 估计Taylor展开的截断误差
     * @param x 计算点
     * @param x0 展开点
     * @param nextDerivative 下一阶导数在x0处的值
     * @param order 当前展开阶数
     * @return 误差估计值
     */
    fun estimateError(x: Double, x0: Double, nextDerivative: Double, order: Int): Double {
        val nextOrder = order + 1
        return abs(nextDerivative) * abs(x - x0).pow(nextOrder) / factorial(nextOrder)
    }
    
    /**
     * 自适应选择Taylor展开的阶数
     * @param x 计算点
     * @param x0 展开点
     * @param derivatives 导数函数列表
     * @param targetError 目标误差
     * @param maxOrder 最大允许阶数
     * @return 合适的展开阶数
     */
    fun adaptiveOrder(
        x: Double,
        x0: Double,
        derivatives: List<(Double) -> Double>,
        targetError: Double,
        maxOrder: Int = 15
    ): Int {
        var order = 1
        while (order < maxOrder) {
            if (order + 1 >= derivatives.size) break
            
            val nextDerivative = derivatives[order + 1](x0)
            val error = estimateError(x, x0, nextDerivative, order)
            
            if (error < targetError) {
                return order
            }
            order++
        }
        return order
    }
    
    /**
 * 生成泰勒展开式的文本表示
 * @param x0 展开点
 * @param derivatives 在x0点的各阶导数值列表[f(x0), f'(x0), f''(x0), ...]
 * @param order 展开阶数
 * @return 格式化的泰勒展开式文本
 */
fun generateTaylorExpansionText(x0: Double, derivatives: List<Double>, order: Int): String {
    val sb = StringBuilder()
    
    // 添加第一项 f(x0)
    sb.append(formatNumber(derivatives[0]))
    
    // 添加剩余项
    for (i in 1..order) {
        if (i < derivatives.size) {
            val derivativeValue = derivatives[i]
            if (derivativeValue != 0.0) {
                val factorialValue = factorial(i)
                val factorialText = if (i > 1) "/$factorialValue" else ""
                
                // 构造(x-x0)部分
                val xMinusX0 = if (x0 == 0.0) "x" else "(x-${formatNumber(x0)})"
                
                // 构造幂部分
                val powerText = if (i > 1) "^$i" else ""
                
                // 构造系数部分
                val coefficientText = formatNumber(derivativeValue)
                
                // 添加符号
                val sign = if (derivativeValue > 0) " + " else " - "
                sb.append(sign)
                
                // 添加完整项
                val absCoefficient = Math.abs(derivativeValue)
                if (absCoefficient == 1.0) {
                    sb.append("$xMinusX0$powerText$factorialText")
                } else {
                    sb.append("$coefficientText·$xMinusX0$powerText$factorialText")
                }
            }
        }
    }
    
    return sb.toString()
}

/**
 * 格式化数字显示
 */
private fun formatNumber(number: Double): String {
    val absNumber = Math.abs(number)
    return when {
        number == 0.0 -> "0"
        absNumber < 0.0001 || absNumber > 10000 -> String.format("%.4e", number)
        number.toInt().toDouble() == number -> number.toInt().toString()
        else -> {
            val formatted = String.format("%.4f", number)
            formatted.replace(Regex("0+$"), "").replace(Regex("\\.$"), "")
        }
    }
}
}