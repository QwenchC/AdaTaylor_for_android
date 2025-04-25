package com.sqq.adataylor.util

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.util.Log
import android.webkit.WebSettings

/**
 * 数学公式显示辅助类
 * 使用KaTeX渲染LaTeX格式的数学公式
 */
class MathFormulaViewer(context: Context) {
    private val webView = WebView(context)
    
    init {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true  // 允许访问文件
        webView.settings.allowContentAccess = true  // 允许访问内容
        // 设置缓存模式
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        // 使用本地存储
        webView.settings.databaseEnabled = true
        
        webView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        
        // 设置调试监听器
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                Log.e("MathFormulaViewer", "WebView加载错误: ${error.description}")
            }
            
            override fun onPageFinished(view: WebView, url: String) {
                // 页面加载完成后，执行渲染
                view.evaluateJavascript("renderMathInElement(document.body);", null)
            }
        }
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
                <!-- KaTeX CSS 加载字体路径修正 -->
                <style>
                    @font-face {
                        font-family: 'KaTeX_Main';
                        src: url('file:///android_asset/katex/fonts/KaTeX_Main-Regular.woff2') format('woff2');
                        font-weight: normal;
                        font-style: normal;
                    }
                    @font-face {
                        font-family: 'KaTeX_Math';
                        src: url('file:///android_asset/katex/fonts/KaTeX_Math-Italic.woff2') format('woff2');
                        font-weight: normal;
                        font-style: italic;
                    }
                    /* 可以添加更多字体声明 */
                    
                    body {
                        font-family: 'KaTeX_Main', serif;
                        margin: 0;
                        padding: 8px;
                        font-size: 16px;
                    }
                    .formula {
                        margin-bottom: 12px;
                    }
                </style>
                <link rel="stylesheet" href="file:///android_asset/katex/katex.min.css">
                <!-- KaTeX JS -->
                <script src="file:///android_asset/katex/katex.min.js"></script>
                <script src="file:///android_asset/katex/auto-render.min.js"></script>
                <script>
                    document.addEventListener("DOMContentLoaded", function() {
                        renderMathInElement(document.body, {
                            delimiters: [
                                {left: "$$", right: "$$", display: true},
                                {left: "$", right: "$", display: false}
                            ],
                            throwOnError: false
                        });
                    });
                </script>
            </head>
            <body>
                <div class="formula">
                    <strong>${functionName}:</strong> $${functionExpression}$
                </div>
                <div class="formula">
                    <strong>泰勒展开式:</strong> $${taylorExpansion}$
                </div>
            </body>
            </html>
        """.trimIndent()
        
        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null)
    }
}