package com.sqq.adataylor.ui.remainder

data class RemainderResult(
    val x: Double,
    val x0: Double,
    val order: Int,
    val exactValue: Double,
    val approximateValue: Double,
    val actualError: Double,
    val remainderValue: Double,
    val remainderType: com.sqq.adataylor.core.RemainderType
)