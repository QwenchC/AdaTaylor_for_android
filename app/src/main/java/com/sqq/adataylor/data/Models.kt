package com.sqq.adataylor.data

import kotlin.math.*

/**
 * 函数数据模型
 */
data class FunctionModel(
    val name: String,
    val expression: String,
    val derivatives: List<String>,
    val derivativeFunctions: List<(Double) -> Double> = emptyList(),
    val mainFunction: (Double) -> Double = { 0.0 },
    val defaultX0: Double = 0.0,
    val domain: Pair<Double, Double> = Pair(-10.0, 10.0)
)

/**
 * 计算结果模型
 */
data class TaylorResult(
    val x: Double,
    val x0: Double,
    val exactValue: Double,
    val approximateValue: Double,
    val error: Double,
    val order: Int,
    val errorEstimate: Double = 0.0
)

/**
 * 可视化数据点
 */
data class DataPoint(
    val x: Double,
    val y: Double
)

/**
 * Taylor展开应用示例
 */
data class TaylorExample(
    val title: String,
    val description: String,
    val formula: String,
    val calculation: String,
    val function: FunctionModel? = null
)

/**
 * 预定义函数工厂
 */
object PredefinedFunctions {
    // 指数函数
    val exponential = FunctionModel(
        name = "指数函数",
        expression = "e^x",
        derivatives = listOf("e^x", "e^x", "e^x", "e^x", "e^x"),
        derivativeFunctions = List(10) { { x -> exp(x) } },
        mainFunction = { x -> exp(x) },
        domain = Pair(-5.0, 5.0)
    )
    
    // 正弦函数
    val sine = FunctionModel(
        name = "正弦函数",
        expression = "sin(x)",
        derivatives = listOf("sin(x)", "cos(x)", "-sin(x)", "-cos(x)", "sin(x)"),
        derivativeFunctions = listOf(
            { x -> sin(x) },
            { x -> cos(x) },
            { x -> -sin(x) },
            { x -> -cos(x) },
            { x -> sin(x) },
            { x -> cos(x) },
            { x -> -sin(x) },
            { x -> -cos(x) },
            { x -> sin(x) },
            { x -> cos(x) }
        ),
        mainFunction = { x -> sin(x) },
        domain = Pair(-2 * PI, 2 * PI)
    )
    
    // 余弦函数
    val cosine = FunctionModel(
        name = "余弦函数",
        expression = "cos(x)",
        derivatives = listOf("cos(x)", "-sin(x)", "-cos(x)", "sin(x)", "cos(x)"),
        derivativeFunctions = listOf(
            { x -> cos(x) },
            { x -> -sin(x) },
            { x -> -cos(x) },
            { x -> sin(x) },
            { x -> cos(x) },
            { x -> -sin(x) },
            { x -> -cos(x) },
            { x -> sin(x) },
            { x -> cos(x) },
            { x -> -sin(x) }
        ),
        mainFunction = { x -> cos(x) },
        domain = Pair(-2 * PI, 2 * PI)
    )
    
    // 对数函数
    val logarithm = FunctionModel(
        name = "自然对数",
        expression = "ln(x)",
        derivatives = listOf("ln(x)", "1/x", "-1/x^2", "2/x^3", "-6/x^4"),
        derivativeFunctions = listOf(
            { x -> ln(x) },
            { x -> 1/x },
            { x -> -1/(x*x) },
            { x -> 2/(x*x*x) },
            { x -> -6/(x*x*x*x) },
            { x -> 24/(x*x*x*x*x) },
            { x -> -120/(x*x*x*x*x*x) },
            { x -> 720/(x.pow(7)) },
            { x -> -5040/(x.pow(8)) },
            { x -> 40320/(x.pow(9)) }
        ),
        mainFunction = { x -> ln(x) },
        defaultX0 = 1.0,
        domain = Pair(0.1, 10.0)
    )

    // 获取所有预定义函数
    fun getAllFunctions(): List<FunctionModel> {
        return listOf(exponential, sine, cosine, logarithm)
    }
}