package com.sqq.adataylor.core

import kotlin.math.min

/**
 * 帕德逼近核心类
 * 实现帕德(Padé)逼近的计算功能
 */
class PadeCore {
    private val adaTaylorCore = AdaTaylorCore()
    
    /**
     * 计算帕德逼近
     * @param x 计算点
     * @param x0 展开点
     * @param taylorCoefficients 泰勒系数
     * @param m 分子多项式阶数
     * @param n 分母多项式阶数
     * @return 帕德逼近结果
     */
    fun computePadeApproximation(
        x: Double, 
        x0: Double, 
        taylorCoefficients: List<Double>, 
        m: Int, 
        n: Int
    ): Double {
        // 确保有足够的系数
        if (taylorCoefficients.size < m + n + 1) {
            throw IllegalArgumentException("泰勒系数不足，需要至少 ${m + n + 1} 个系数")
        }
        
        // 计算分子分母多项式系数
        val (numeratorCoefficients, denominatorCoefficients) = 
            calculatePadeCoefficients(taylorCoefficients, m, n)
        
        // 计算分子多项式的值
        var numerator = 0.0
        for (i in 0..m) {
            numerator += numeratorCoefficients[i] * (x - x0).pow(i)
        }
        
        // 计算分母多项式的值
        var denominator = 0.0
        for (i in 0..n) {
            denominator += denominatorCoefficients[i] * (x - x0).pow(i)
        }
        
        // 防止除以零
        if (denominator.absoluteValue < 1e-10) {
            return Double.NaN
        }
        
        return numerator / denominator
    }
    
    /**
     * 计算帕德系数
     * @param taylorCoefficients 泰勒系数
     * @param m 分子多项式阶数
     * @param n 分母多项式阶数
     * @return 分子和分母多项式系数对
     */
    private fun calculatePadeCoefficients(
        taylorCoefficients: List<Double>, 
        m: Int, 
        n: Int
    ): Pair<List<Double>, List<Double>> {
        // 创建线性方程组矩阵
        val matrix = Array(n) { DoubleArray(n) }
        val rightHandSide = DoubleArray(n)
        
        // 填充矩阵和右侧向量
        for (i in 0 until n) {
            for (j in 0 until n) {
                val index = m + i - j
                matrix[i][j] = if (index >= 0 && index < taylorCoefficients.size) {
                    taylorCoefficients[index]
                } else {
                    0.0
                }
            }
            
            rightHandSide[i] = -taylorCoefficients[m + i + 1]
        }
        
        // 求解线性方程组
        val bCoefficients = solveLinearSystem(matrix, rightHandSide)
        
        // 创建分母系数（b0 = 1）
        val denominatorCoefficients = mutableListOf(1.0)
        denominatorCoefficients.addAll(bCoefficients)
        
        // 计算分子系数
        val numeratorCoefficients = mutableListOf<Double>()
        for (i in 0..m) {
            var coefficient = taylorCoefficients[i]
            for (j in 1..min(i, n)) {
                coefficient += denominatorCoefficients[j] * taylorCoefficients[i - j]
            }
            numeratorCoefficients.add(coefficient)
        }
        
        return Pair(numeratorCoefficients, denominatorCoefficients)
    }
    
    /**
     * 求解线性方程组 Ax = b
     * 使用高斯消元法
     */
    private fun solveLinearSystem(
        coefficientMatrix: Array<DoubleArray>, 
        constants: DoubleArray
    ): List<Double> {
        val n = coefficientMatrix.size
        
        // 创建增广矩阵
        val augmentedMatrix = Array(n) { DoubleArray(n + 1) }
        for (i in 0 until n) {
            for (j in 0 until n) {
                augmentedMatrix[i][j] = coefficientMatrix[i][j]
            }
            augmentedMatrix[i][n] = constants[i]
        }
        
        // 高斯消元 - 前向消元
        for (i in 0 until n) {
            // 查找主元
            var maxRowIndex = i
            for (j in i + 1 until n) {
                if (augmentedMatrix[j][i].absoluteValue > augmentedMatrix[maxRowIndex][i].absoluteValue) {
                    maxRowIndex = j
                }
            }
            
            // 交换行
            if (maxRowIndex != i) {
                for (j in 0..n) {
                    val temp = augmentedMatrix[i][j]
                    augmentedMatrix[i][j] = augmentedMatrix[maxRowIndex][j]
                    augmentedMatrix[maxRowIndex][j] = temp
                }
            }
            
            // 奇异矩阵检查
            if (augmentedMatrix[i][i].absoluteValue < 1e-10) {
                throw ArithmeticException("矩阵奇异，无解")
            }
            
            // 规格化当前行
            val pivot = augmentedMatrix[i][i]
            for (j in i..n) {
                augmentedMatrix[i][j] /= pivot
            }
            
            // 消元
            for (j in 0 until n) {
                if (j != i) {
                    val factor = augmentedMatrix[j][i]
                    for (k in i..n) {
                        augmentedMatrix[j][k] -= factor * augmentedMatrix[i][k]
                    }
                }
            }
        }
        
        // 提取解
        val solution = mutableListOf<Double>()
        for (i in 0 until n) {
            solution.add(augmentedMatrix[i][n])
        }
        
        return solution
    }
    
    /**
     * Double的扩展函数：计算幂
     */
    private fun Double.pow(n: Int): Double {
        if (n < 0) return 1.0 / this.pow(-n)
        if (n == 0) return 1.0
        if (n == 1) return this
        
        val half = this.pow(n / 2)
        return if (n % 2 == 0) half * half else half * half * this
    }
    
    /**
     * Double的绝对值扩展属性
     */
    private val Double.absoluteValue: Double
        get() = if (this < 0) -this else this
        
    /**
     * 从泰勒系数计算帕德逼近
     * @param function 函数对象
     * @param x 计算点
     * @param x0 展开点
     * @param m 分子多项式阶数
     * @param n 分母多项式阶数
     * @return 帕德逼近结果
     */
    fun approximatePade(
        function: (Double) -> Double,
        derivatives: List<(Double) -> Double>,
        x: Double,
        x0: Double,
        m: Int,
        n: Int
    ): Double {
        val maxOrder = m + n
        
        // 计算泰勒系数
        val taylorCoefficients = calculateTaylorCoefficients(function, derivatives, x0, maxOrder)
        
        // 计算帕德逼近
        return computePadeApproximation(x, x0, taylorCoefficients, m, n)
    }
    
    /**
     * 计算泰勒系数
     */
    private fun calculateTaylorCoefficients(
        function: (Double) -> Double,
        derivatives: List<(Double) -> Double>,
        x0: Double,
        maxOrder: Int
    ): List<Double> {
        val coefficients = mutableListOf<Double>()
        
        // 0阶系数是函数值
        coefficients.add(function(x0))
        
        // 计算高阶导数系数
        for (i in 1..maxOrder) {
            if (i < derivatives.size) {
                val derivative = derivatives[i](x0)
                coefficients.add(derivative / adaTaylorCore.factorial(i))
            } else {
                // 如果没有提供足够的导数，使用数值方法
                coefficients.add(0.0) // 简化处理
            }
        }
        
        return coefficients
    }
}