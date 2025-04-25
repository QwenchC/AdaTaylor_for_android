package com.sqq.adataylor.util

/**
 * TeX格式转换工具
 * 将普通数学表达式转换为TeX格式
 */
object TeXConverter {
    /**
     * 将普通数学表达式转换为TeX格式
     */
    fun toTex(expression: String): String {
        var texExpression = expression
        
        // 替换常见的数学函数
        texExpression = texExpression.replace("sin(", "\\sin(")
        texExpression = texExpression.replace("cos(", "\\cos(")
        texExpression = texExpression.replace("tan(", "\\tan(")
        texExpression = texExpression.replace("ln(", "\\ln(")
        texExpression = texExpression.replace("log(", "\\log(")
        texExpression = texExpression.replace("exp(", "\\exp(")
        texExpression = texExpression.replace("e^x", "e^{x}")
        texExpression = texExpression.replace("sqrt(", "\\sqrt{")
        texExpression = texExpression.replace("pi", "\\pi")
        
        // 替换幂运算
        val powerRegex = Regex("([a-zA-Z0-9]+)\\^([a-zA-Z0-9]+)")
        texExpression = powerRegex.replace(texExpression) { matchResult ->
            val base = matchResult.groupValues[1]
            val exponent = matchResult.groupValues[2]
            "$base^{$exponent}"
        }
        
        // 替换分数
        val fractionRegex = Regex("([a-zA-Z0-9]+)/([a-zA-Z0-9]+)")
        texExpression = fractionRegex.replace(texExpression) { matchResult ->
            val numerator = matchResult.groupValues[1]
            val denominator = matchResult.groupValues[2]
            "\\frac{$numerator}{$denominator}"
        }
        
        // 替换括号
        texExpression = texExpression.replace("(", "\\left(")
        texExpression = texExpression.replace(")", "\\right)")
        
        return texExpression
    }
}