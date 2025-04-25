package com.sqq.adataylor.core

import kotlin.math.pow

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
    private fun factorial(n: Int): Double {
        var result = 1.0
        for (i in 2..n) {
            result *= i
        }
        return result
    }
}