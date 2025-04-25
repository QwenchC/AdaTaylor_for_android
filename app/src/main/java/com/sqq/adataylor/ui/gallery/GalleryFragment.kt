package com.sqq.adataylor.ui.gallery

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sqq.adataylor.data.CustomFunctionHelper
import com.sqq.adataylor.data.FunctionManager
import com.sqq.adataylor.data.FunctionModel
import com.sqq.adataylor.databinding.DialogCustomFunctionBinding
import com.sqq.adataylor.databinding.FragmentGalleryBinding
import com.sqq.adataylor.ui.home.HomeViewModel
import com.sqq.adataylor.util.MathFormulaViewer
import com.sqq.adataylor.util.TeXConverter

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var homeViewModel: HomeViewModel
    private var selectedFunction: FunctionModel? = null
    private var currentOrder = 3
    private val customFunctionHelper = CustomFunctionHelper()
    private var customFunction: FunctionModel? = null

    // 在类级别声明
    private lateinit var mathFormulaViewer: MathFormulaViewer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        galleryViewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        galleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        setupFunctionSpinner()
        setupOrderSeekBar()
        setupPlotButton()
        setupCustomFunctionButton()

        // 添加下面这行，确保初始显示函数表达式
        selectedFunction?.let {
            binding.textFunctionExpression.text = "f(x) = ${it.expression}"
        }

        mathFormulaViewer = MathFormulaViewer(requireContext())
        binding.formulaContainer.addView(mathFormulaViewer.getWebView())
        
        // 检查是否有公式渲染错误
        try {
            // 测试简单公式
            mathFormulaViewer.displayFunctionAndTaylor(
                "测试", 
                "x^2",
                "x^2"
            )
        } catch (e: Exception) {
            Log.e("MathRender", "公式渲染初始化失败: ${e.message}", e)
            Toast.makeText(requireContext(), "公式渲染初始化失败", Toast.LENGTH_SHORT).show()
        }
        
        return root
    }

    private fun setupCustomFunctionButton() {
        binding.buttonCustomFunction.setOnClickListener {
            showCustomFunctionDialog()
        }
    }

    private fun showCustomFunctionDialog() {
        val dialogBinding = DialogCustomFunctionBinding.inflate(LayoutInflater.from(context))
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("创建") { _, _ ->
                createCustomFunction(dialogBinding)
            }
            .setNegativeButton("取消", null)
            .create()
        
        // 测试函数按钮
        dialogBinding.buttonTestFunction.setOnClickListener {
            val expression = dialogBinding.editFunctionExpression.text.toString()
            val func = customFunctionHelper.createFunction(expression)
            
            if (func != null) {
                try {
                    val testValue = func(1.0)
                    dialogBinding.textTestResult.visibility = View.VISIBLE
                    dialogBinding.textTestResult.text = "函数测试: f(1) = $testValue"
                } catch (e: Exception) {
                    dialogBinding.textTestResult.visibility = View.VISIBLE
                    dialogBinding.textTestResult.text = "函数测试失败: ${e.message}"
                }
            } else {
                dialogBinding.textTestResult.visibility = View.VISIBLE
                dialogBinding.textTestResult.text = "函数表达式无效"
            }
        }
        
        dialog.show()
    }

    // 修改createCustomFunction方法
    private fun createCustomFunction(dialogBinding: DialogCustomFunctionBinding) {
        val name = dialogBinding.editFunctionName.text.toString()
        val expression = dialogBinding.editFunctionExpression.text.toString()
        val x0 = dialogBinding.editExpansionPoint.text.toString().toDoubleOrNull() ?: 0.0
        val minDomain = dialogBinding.editDomainMin.text.toString().toDoubleOrNull() ?: -10.0
        val maxDomain = dialogBinding.editDomainMax.text.toString().toDoubleOrNull() ?: 10.0
        
        val function = customFunctionHelper.createCustomFunction(
            name, 
            expression, 
            x0,
            Pair(minDomain, maxDomain)
        )
        
        if (function != null) {
            // 设置到函数管理器
            FunctionManager.setCustomFunction(function)
            customFunction = function
            
            // 更新Spinner
            setupFunctionSpinner()
            
            // 找到新函数位置并选中
            val functions = FunctionManager.getAllFunctions()
            val index = functions.indexOfFirst { it.name == function.name }
            if (index >= 0 && index < binding.spinnerFunction.adapter.count) {
                binding.spinnerFunction.setSelection(index)
            }
            
            Toast.makeText(context, "自定义函数创建成功", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "自定义函数创建失败，请检查表达式", Toast.LENGTH_SHORT).show()
        }
    }

    // 修改setupFunctionSpinner方法
    private fun setupFunctionSpinner() {
        // 从函数管理器获取所有函数
        val functions = FunctionManager.getAllFunctions()
        val functionNames = functions.map { it.name }
        
        val adapter = ArrayAdapter(
            requireContext(), 
            android.R.layout.simple_spinner_item, 
            functionNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.spinnerFunction.adapter = adapter
        binding.spinnerFunction.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position < functions.size) {
                    selectedFunction = functions[position]
                    // 更新函数表达式显示
                    binding.textFunctionExpression.text = "f(x) = ${selectedFunction?.expression ?: ""}"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedFunction = null
                binding.textFunctionExpression.text = ""
            }
        }
    }

    private fun setupOrderSeekBar() {
        binding.seekbarOrder.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentOrder = progress
                binding.textOrder.text = "阶数: $currentOrder"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupPlotButton() {
        binding.buttonPlot.setOnClickListener {
            val start = binding.editRangeStart.text.toString().toDoubleOrNull()
            val end = binding.editRangeEnd.text.toString().toDoubleOrNull()
            val x0 = binding.editX0.text.toString().toDoubleOrNull()

            if (start == null || end == null || x0 == null || selectedFunction == null) {
                Toast.makeText(context, "请输入有效的范围和展开点", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (start >= end) {
                Toast.makeText(context, "起始值必须小于结束值", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            plotFunction(start, end, x0)
        }
    }

    private fun setupChart() {
        val chart = binding.chart
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.legend.isEnabled = true
        
        // 启用缩放和拖动
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
    }

    // 修改plotFunction方法
    private fun plotFunction(start: Double, end: Double, x0: Double) {
        val (exactPoints, taylorPoints) = galleryViewModel.generateFunctionPoints(
            selectedFunction!!,
            start,
            end,
            x0,
            currentOrder
        )
        
        // 显示公式格式的泰勒展开式
        val latexExpression = TeXConverter.toTex(selectedFunction?.expression ?: "")
        val taylorLatex = galleryViewModel.getTaylorExpansionLatex(selectedFunction!!, x0, currentOrder)
        
        mathFormulaViewer.displayFunctionAndTaylor(
            selectedFunction!!.name,
            latexExpression,
            taylorLatex,
            currentOrder // 传递展开阶数
        )
        
        // 绘制图表 - 原有代码
        // 转换为Entry对象
        val exactEntries = exactPoints.map { Entry(it.x.toFloat(), it.y.toFloat()) }
        val taylorEntries = taylorPoints.map { Entry(it.x.toFloat(), it.y.toFloat()) }
        
        // 创建LineDataSet
        val exactDataSet = LineDataSet(exactEntries, "精确值").apply {
            color = Color.BLUE
            setDrawCircles(false)
            lineWidth = 2f
        }
        
        val taylorDataSet = LineDataSet(taylorEntries, "Taylor展开(阶数:$currentOrder)").apply {
            color = Color.RED
            setDrawCircles(false)
            lineWidth = 2f
            enableDashedLine(10f, 5f, 0f)
        }
        
        // 创建LineData并设置给Chart
        val lineData = LineData(exactDataSet, taylorDataSet)
        binding.chart.data = lineData
        binding.chart.invalidate() // 刷新图表
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}