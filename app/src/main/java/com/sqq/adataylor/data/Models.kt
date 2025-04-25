package com.sqq.adataylor.data

/**
 * 函数数据模型
 */
data class FunctionModel(
    val name: String,
    val expression: String,
    val derivatives: List<String>,
    val defaultX0: Double = 0.0
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
    val order: Int
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
    val calculation: String
)