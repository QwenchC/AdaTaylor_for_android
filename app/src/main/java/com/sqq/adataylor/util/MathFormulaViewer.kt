package com.sqq.adataylor.util

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.util.Base64

/**
 * 数学公式显示辅助类
 * 使用KaTeX渲染LaTeX格式的数学公式
 */
class MathFormulaViewer(private val context: Context) {
    private val webView = WebView(context)
    
    init {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
        }
        
        webView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // 页面加载完成后执行渲染
                view.evaluateJavascript(
                    "if (typeof renderMathInElement === 'function') { renderMathInElement(document.body); }",
                    null
                )
            }
        }
    }
    
    fun getWebView(): WebView = webView
    
    /**
     * 显示函数表达式和泰勒展开式
     */
    fun displayFunctionAndTaylor(
        functionName: String, 
        functionExpression: String, 
        taylorExpansion: String,
        order: Int = 0
    ) {
        // 将LaTeX代码转换为Base64以完全避免转义问题
        val functionBase64 = Base64.encodeToString(functionExpression.toByteArray(), Base64.NO_WRAP)
        val taylorBase64 = Base64.encodeToString(taylorExpansion.toByteArray(), Base64.NO_WRAP)
        
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css">
                <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js"></script>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 0;
                        padding: 8px;
                        font-size: 16px;
                    }
                    .formula-container {
                        margin-bottom: 12px;
                    }
                    .function-title {
                        font-weight: bold;
                    }
                    .formula-display {
                        padding: 4px 0;
                    }
                </style>
            </head>
            <body>
                <div class="formula-container">
                    <div class="function-title">${functionName}:</div>
                    <div class="formula-display" id="function-formula"></div>
                </div>
                <div class="formula-container">
                    <div class="function-title">泰勒展开式:</div>
                    <div class="formula-display" id="taylor-formula"></div>
                </div>
                
                <script>
                    window.onload = function() {
                        try {
                            // 解码Base64数据
                            var functionLatex = atob("$functionBase64");
                            var taylorLatex = atob("$taylorBase64");
                            
                            katex.render(
                                "f(x) = " + functionLatex,
                                document.getElementById("function-formula"),
                                { displayMode: true, throwOnError: false }
                            );
                            
                            katex.render(
                                "T_{$order}(x) = " + taylorLatex + " + o((x-x_0)^{$order})",
                                document.getElementById("taylor-formula"),
                                { displayMode: true, throwOnError: false }
                            );
                        } catch (e) {
                            console.error("渲染错误:", e);
                            document.getElementById("function-formula").textContent = "公式渲染错误: " + e.message;
                        }
                    };
                </script>
            </body>
            </html>
        """.trimIndent()
        
        webView.loadDataWithBaseURL(
            "https://cdn.jsdelivr.net/",
            html,
            "text/html",
            "UTF-8",
            null
        )
    }
    
    /**
     * 显示函数表达式、泰勒展开式和余项公式
     */
    fun displayFunctionAndRemainderFormula(
        functionName: String, 
        functionExpression: String, 
        taylorExpansion: String,
        remainderFormula: String,
        order: Int = 0,
        remainderTypeName: String = ""
    ) {
        // 将LaTeX代码转换为Base64以完全避免转义问题
        val functionBase64 = Base64.encodeToString(functionExpression.toByteArray(), Base64.NO_WRAP)
        val taylorBase64 = Base64.encodeToString(taylorExpansion.toByteArray(), Base64.NO_WRAP)
        val remainderBase64 = Base64.encodeToString(remainderFormula.toByteArray(), Base64.NO_WRAP)
        
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css">
                <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js"></script>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 0;
                        padding: 8px;
                        font-size: 16px;
                    }
                    .formula-container {
                        margin-bottom: 12px;
                    }
                    .function-title {
                        font-weight: bold;
                    }
                    .formula-display {
                        padding: 4px 0;
                    }
                </style>
            </head>
            <body>
                <div class="formula-container">
                    <div class="function-title">${functionName}:</div>
                    <div class="formula-display" id="function-formula"></div>
                </div>
                <div class="formula-container">
                    <div class="function-title">泰勒展开式:</div>
                    <div class="formula-display" id="taylor-formula"></div>
                </div>
                <div class="formula-container">
                    <div class="function-title">${remainderTypeName}余项:</div>
                    <div class="formula-display" id="remainder-formula"></div>
                </div>
                
                <script>
                    window.onload = function() {
                        try {
                            // 解码Base64数据
                            var functionLatex = atob("$functionBase64");
                            var taylorLatex = atob("$taylorBase64");
                            var remainderLatex = atob("$remainderBase64");
                            
                            katex.render(
                                "f(x) = " + functionLatex,
                                document.getElementById("function-formula"),
                                { displayMode: true, throwOnError: false }
                            );
                            
                            katex.render(
                                "T_{$order}(x) = " + taylorLatex + " + R_{$order}(x)",
                                document.getElementById("taylor-formula"),
                                { displayMode: true, throwOnError: false }
                            );
                            
                            katex.render(
                                remainderLatex,
                                document.getElementById("remainder-formula"),
                                { displayMode: true, throwOnError: false }
                            );
                        } catch (e) {
                            console.error("渲染错误:", e);
                            document.getElementById("function-formula").textContent = "公式渲染错误: " + e.message;
                        }
                    };
                </script>
            </body>
            </html>
        """.trimIndent()
        
        webView.loadDataWithBaseURL(
            "https://cdn.jsdelivr.net/",
            html,
            "text/html",
            "UTF-8",
            null
        )
    }
}