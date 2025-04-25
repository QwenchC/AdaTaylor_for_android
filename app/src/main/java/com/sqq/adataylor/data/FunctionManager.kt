package com.sqq.adataylor.data

/**
 * 函数管理器单例
 * 用于在不同页面间共享自定义函数
 */
object FunctionManager {
    // 预定义函数列表
    private val predefinedFunctions = PredefinedFunctions.getAllFunctions()
    
    // 自定义函数
    private var customFunction: FunctionModel? = null
    
    // 获取所有函数（预定义+自定义）
    fun getAllFunctions(): List<FunctionModel> {
        val result = predefinedFunctions.toMutableList()
        customFunction?.let { result.add(it) }
        return result
    }
    
    // 添加或更新自定义函数
    fun setCustomFunction(function: FunctionModel) {
        customFunction = function
    }
    
    // 获取自定义函数
    fun getCustomFunction(): FunctionModel? = customFunction
    
    // 获取预定义函数
    fun getPredefinedFunctions(): List<FunctionModel> = predefinedFunctions
}