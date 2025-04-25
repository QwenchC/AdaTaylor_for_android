package com.sqq.adataylor.data

import kotlin.math.*
import net.objecthunter.exp4j.ExpressionBuilder

/**
 * 自定义函数辅助类
 * 用于解析和计算用户输入的数学表达式
 */
class CustomFunctionHelper {
    
    /**
     * 从字符串表达式创建函数
     */
    fun createFunction(expression: String): ((Double) -> Double)? {
        return try {
            // 使用lambda包装表达式计算
            { x ->
                val exp = ExpressionBuilder(expression)
                    .variables(setOf("x"))
                    .build()
                exp.setVariable("x", x)
                exp.evaluate()
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // 数值导数计算函数
    fun numericalDerivative(
        func: (Double) -> Double,
        order: Int,
        h: Double = 0.0001
    ): (Double) -> Double {
        if (order == 0) return func
        
        if (order == 1) {
            return { x -> (func(x + h) - func(x - h)) / (2 * h) }
        }
        
        if (order == 2) {
            return { x -> (func(x + h) - 2 * func(x) + func(x - h)) / (h * h) }
        }
        
        if (order == 3) {
            return { x -> 
                (func(x + 2*h) - 2*func(x + h) + 2*func(x - h) - func(x - 2*h)) / (2 * h * h * h)
            }
        }
        
        if (order == 4) {
            return { x ->
                (func(x + 2*h) - 4*func(x + h) + 6*func(x) - 4*func(x - h) + func(x - 2*h)) / (h * h * h * h)
            }
        }
        
        // 对于更高阶导数，通过先计算一阶导数，然后递归地对其求导
        val firstDerivative = numericalDerivative(func, 1, h)
        return numericalDerivative(firstDerivative, order - 1, h)
    }
    
    /**
     * 创建自定义函数模型
     */
    fun createCustomFunction(
        name: String,
        expression: String,
        x0: Double,
        domain: Pair<Double, Double>
    ): FunctionModel? {
        val mainFunc = createFunction(expression) ?: return null
        
        // 创建导数函数列表
        val derivativeFunctions = mutableListOf<(Double) -> Double>()
        derivativeFunctions.add(mainFunc)
        
        // 添加30个导数函数
        for (i in 1..29) {
            val derivFunc: (Double) -> Double = numericalDerivative(mainFunc, i)
            derivativeFunctions.add(derivFunc)
        }
        
        // 创建导数表达式描述列表
        val derivatives = mutableListOf<String>()
        derivatives.add(expression)
        derivatives.add("d/dx($expression)")
        derivatives.add("d²/dx²($expression)")
        derivatives.add("d³/dx³($expression)")
        for (i in 4..9) {
            derivatives.add("d^$i/dx^$i($expression)")
        }
        
        return FunctionModel(
            name = name,
            expression = expression,
            derivatives = derivatives,
            derivativeFunctions = derivativeFunctions,
            mainFunction = mainFunc,
            defaultX0 = x0,
            domain = domain
        )
    }
}