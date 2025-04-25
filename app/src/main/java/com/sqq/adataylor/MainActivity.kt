package com.sqq.adataylor

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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

        // 替换 FAB 的点击监听器
        binding.appBarMain.fab.setOnClickListener {
            // 创建并显示学生信息对话框
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
                // 可选：提供触觉反馈表示进入拖动模式
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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}