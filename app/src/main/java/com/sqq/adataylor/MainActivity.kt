package com.sqq.adataylor

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.app.Dialog
import android.view.Window
import android.widget.Button
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.sqq.adataylor.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.text.Html
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // FAB 的点击监听器
        binding.appBarMain.fab.setOnClickListener {
            showStudentInfoDialog()
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // 在 onCreate 或 onViewCreated 方法中添加这段代码
        val fab = findViewById<FloatingActionButton>(R.id.fab)

        fab.setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0f
            private var initialTouchY: Float = 0f
            private var longPressed = false
            private val longPressTimeout = 500 // 长按时间阈值（毫秒）

            @SuppressLint("ClickableViewAccessibility")
            private val longPressRunnable = Runnable {
                longPressed = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    fab.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
            }

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 记录初始位置
                        initialX = fab.x.toInt()
                        initialY = fab.y.toInt()
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY

                        // 设置长按检测
                        fab.postDelayed(longPressRunnable, longPressTimeout.toLong())
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (longPressed) {
                            // 计算位移并更新位置
                            val newX = initialX + (event.rawX - initialTouchX).toInt()
                            val newY = initialY + (event.rawY - initialTouchY).toInt()

                            // 确保按钮不会移出屏幕
                            val parent = fab.parent as ViewGroup
                            val maxX = parent.width - fab.width
                            val maxY = parent.height - fab.height

                            fab.x = newX.coerceIn(0, maxX).toFloat()
                            fab.y = newY.coerceIn(0, maxY).toFloat()
                            return true
                        }
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // 取消长按检测
                        fab.removeCallbacks(longPressRunnable)

                        // 如果不是长按状态，则执行点击
                        if (!longPressed) {
                            fab.performClick()
                        }

                        longPressed = false
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun showStudentInfoDialog() {
        // 添加字体大小样式
        val htmlMessage = """
            <div style="font-size:18px">
                <b>学生姓名：</b><span style="color:#673AB7">孙钦奇</span><br>
                <b>学号：</b>2100802107<br>
                <b>专业：</b>数学与应用数学<br>
                <b>班级：</b>21 数学 1 班<br>
                <b>设计（论文）题目：</b><br>
                《泰勒公式及其在近似计算中的应用拓展》<br>
                <b>指导教师：</b>汪悦<br>
                <b>学院名称：</b>理学院
            </div>
        """.trimIndent()
        
        // 将HTML转换为可显示的富文本
        val spannedMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlMessage, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(htmlMessage)
        }
        
        // 创建并显示对话框，设置更大的文字尺寸
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("关于作者")
            .setMessage(spannedMessage)
            .setPositiveButton("确定", null)
            .show()
        
        // 获取消息文本视图并设置其文字大小
        val messageView = dialog.findViewById<TextView>(android.R.id.message)
        messageView?.textSize = 16f  // 设置为16sp的文字大小
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_theory -> {
                showTheoryContent()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showTheoryContent() {
        // 使用WebView创建一个对话框
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_theory_content)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        
        val webView = dialog.findViewById<WebView>(R.id.webview_theory)
        webView.settings.javaScriptEnabled = true
        
        // 加载HTML内容
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link rel="stylesheet" href="file:///android_asset/katex/katex.min.css">
                <script src="file:///android_asset/katex/katex.min.js"></script>
                <script src="file:///android_asset/katex/auto-render.min.js"></script>
                <style>
                    body { 
                        padding: 12px; 
                        font-family: 'Arial', sans-serif;
                        line-height: 1.6;
                    }
                    h1 { font-size: 1.8em; color: #333; margin-top: 20px; }
                    h2 { font-size: 1.5em; color: #444; margin-top: 18px; }
                    h3 { font-size: 1.3em; color: #555; margin-top: 16px; }
                    .theorem { 
                        background-color: #f8f9fa; 
                        padding: 10px; 
                        border-left: 4px solid #673AB7; 
                        margin: 10px 0;
                    }
                    .proof {
                        font-style: italic;
                        margin-left: 20px;
                    }
                    code {
                        background-color: #f5f5f5;
                        padding: 2px 5px;
                        border-radius: 3px;
                        font-family: monospace;
                    }
                </style>
            </head>
            <body>
                <h1>AdaTaylor：自适应泰勒展开逼近理论与方法</h1>
                
                <h2>1. 自适应阶数选择的数学理论</h2>
                
                <h3>1.1 泰勒展开的余项理论</h3>
                <p>对于在点\(x_0\)邻域内\(n+1\)阶可导的函数\(f(x)\)，其\(n\)阶泰勒展开可表示为：</p>
                <p>\[f(x) = \sum_{k=0}^{n}\frac{f^{(k)}(x_0)}{k!}(x-x_0)^k + R_n(x)\]</p>
                <p>其中\(R_n(x)\)为余项，可用拉格朗日形式表示：</p>
                <p>\[R_n(x) = \frac{f^{(n+1)}(\xi)}{(n+1)!}(x-x_0)^{n+1}\]</p>
                <p>其中\(\xi\)在\(x_0\)与\(x\)之间。若在区间\([a,b]\)上有\(\left|f^{(n+1)}(x)\right| \leq M_{n+1}\)，则余项的绝对值满足：</p>
                <p>\[|R_n(x)| \leq \frac{M_{n+1}}{(n+1)!}|x-x_0|^{n+1}\]</p>
                
                <h3>1.2 自适应阶数选择的数学准则</h3>
                <p>给定误差容限\(\varepsilon\)和定义域\([a,b]\)，AdaTaylor系统基于以下数学准则自动选择展开阶数：</p>
                
                <div class="theorem">
                    <strong>定理1</strong> (自适应阶数选择准则)：对于在区间\([a,b]\)上\(n+1\)阶可导的函数\(f(x)\)，若要求泰勒展开在整个区间上的误差不超过\(\varepsilon\)，则所需的最小展开阶数\(n\)满足：
                    <p>\[\frac{M_{n+1}}{(n+1)!}d^{n+1} < \varepsilon\]</p>
                    <p>其中\(d = \max\{|a-x_0|, |b-x_0|\}\)，\(M_{n+1}\)为\(|f^{(n+1)}(x)|\)在区间\([a,b]\)上的上界。</p>
                </div>
                
                <div class="proof">
                    <strong>证明</strong>：根据拉格朗日余项公式，对于区间\([a,b]\)内任意点\(x\)，有
                    \(|R_n(x)| \leq \frac{M_{n+1}}{(n+1)!}|x-x_0|^{n+1}\)。
                    由于\(|x-x_0| \leq d\)，因此\(|R_n(x)| \leq \frac{M_{n+1}}{(n+1)!}d^{n+1}\)。
                    当\(\frac{M_{n+1}}{(n+1)!}d^{n+1} < \varepsilon\)时，区间内所有点的误差都小于\(\varepsilon\)。\(\square\)
                </div>

                <p>更多内容见毕设论文……</p>
                
            </body>
            <script>
                document.addEventListener("DOMContentLoaded", function() {
                    renderMathInElement(document.body, {
                        delimiters: [
                            {left: "$$", right: "$$", display: true},
                            {left: "$", right: "$", display: false},
                            {left: "\\[", right: "\\]", display: true},
                            {left: "\\(", right: "\\)", display: false}
                        ]
                    });
                });
            </script>
            </html>
        """.trimIndent()
        
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        
        // 关闭按钮
        val closeButton = dialog.findViewById<Button>(R.id.button_close_theory)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}