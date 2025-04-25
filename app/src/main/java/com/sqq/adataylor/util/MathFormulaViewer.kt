package com.sqq.adataylor.util

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout

/**
 * 数学公式显示辅助类
 * 使用MathJax渲染LaTeX格式的数学公式
 */
class MathFormulaViewer(context: Context) {
    private val webView = WebView(context)
    
    init {
        webView.settings.javaScriptEnabled = true
        webView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        webView.webViewClient = WebViewClient()
    }
    
    /**
     * 获取WebView实例
     */
    fun getWebView(): WebView = webView
    
    /**
     * 显示函数表达式和泰勒展开式
     */
    fun displayFunctionAndTaylor(functionName: String, functionExpression: String, taylorExpansion: String) {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <script type="text/javascript" id="MathJax-script" async
                    src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js">
                </script>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 0;
                        padding: 8px;
                        font-size: 16px;
                    }
                    .formula {
                        margin-bottom: 8px;
                    }
                </style>
            </head>
            <body>
                <div class="formula">
                    <strong>$functionName:</strong> \$$functionExpression\$
                </div>
                <div class="formula">
                    <strong>泰勒展开式:</strong> \$$taylorExpansion\$
                </div>
            </body>
            </html>
        """.trimIndent()
        
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
}