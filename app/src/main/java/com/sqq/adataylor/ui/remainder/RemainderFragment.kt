package com.sqq.adataylor.ui.remainder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.sqq.adataylor.R
import com.sqq.adataylor.core.RemainderType
import com.sqq.adataylor.databinding.FragmentRemainderBinding
import com.sqq.adataylor.data.FunctionModel
import com.sqq.adataylor.data.FunctionManager  // 修改: FunctionRepository -> FunctionManager
import com.sqq.adataylor.util.MathFormulaViewer
import com.sqq.adataylor.util.TeXConverter
import java.text.DecimalFormat
import kotlin.math.min

class RemainderFragment : Fragment() {
    
    private var _binding: FragmentRemainderBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var remainderViewModel: RemainderViewModel
    private var selectedFunction: FunctionModel? = null
    private lateinit var mathFormulaViewer: MathFormulaViewer
    private val decimalFormat = DecimalFormat("#.########")
    
    // 余项类型选项
    private val remainderTypes = listOf(
        RemainderType.LAGRANGE,
        RemainderType.CAUCHY,
        RemainderType.INTEGRAL
    )
    
    private var currentRemainderType = RemainderType.LAGRANGE
    private var currentOrder = 3
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        remainderViewModel = ViewModelProvider(this)[RemainderViewModel::class.java]
        
        _binding = FragmentRemainderBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        mathFormulaViewer = MathFormulaViewer(requireContext())
        binding.formulaContainer.addView(mathFormulaViewer.getWebView())
        
        setupFunctionSpinner()
        setupRemainderTypeSpinner()
        setupOrderSlider()
        setupCalculateButton()
        setupChart()
        
        return root
    }
    
    private fun setupFunctionSpinner() {
        val functions = FunctionManager.getAllFunctions()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            functions.map { function -> function.name }  // 显式命名参数，避免类型推断问题
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFunction.adapter = adapter
        
        binding.spinnerFunction.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedFunction = functions[position]
                binding.textFunctionExpression.text = selectedFunction?.expression
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 不做任何事情
            }
        }
        
        // 修复自定义函数按钮
        binding.buttonCustomFunction.setOnClickListener {
            // 简化处理，只显示提示
            Toast.makeText(context, "目前暂不支持自定义函数", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupRemainderTypeSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            remainderTypes.map { it.displayName }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRemainderType.adapter = adapter
        
        binding.spinnerRemainderType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentRemainderType = remainderTypes[position]
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 不做任何事情
            }
        }
    }
    
    // 修改 setupOrderSlider 方法
    private fun setupOrderSlider() {
        binding.seekbarOrder.max = 8  // 最大支持8阶
        binding.textOrder.text = "展开阶数：$currentOrder"
        binding.seekbarOrder.progress = currentOrder - 1
        
        binding.seekbarOrder.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentOrder = progress + 1
                binding.textOrder.text = "展开阶数：$currentOrder"
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupCalculateButton() {
        binding.buttonCalculate.setOnClickListener {
            if (selectedFunction == null) {
                Toast.makeText(context, "请先选择一个函数", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            try {
                // 获取输入值
                val x = binding.editX.text.toString().toDoubleOrNull() ?: 0.0
                val x0 = binding.editX0.text.toString().toDoubleOrNull() ?: 0.0
                
                // 计算余项
                val result = remainderViewModel.calculateRemainder(
                    selectedFunction!!,
                    x,
                    x0,
                    currentOrder,
                    currentRemainderType
                )
                
                // 显示结果
                displayResults(result)
                
                // 分析不同阶数的余项变化，最大不超过8阶
                val maxAnalysisOrder = minOf(8, selectedFunction!!.derivativeFunctions.size - 1)
                val analysisData = remainderViewModel.analyzeRemainderByOrder(
                    selectedFunction!!,
                    x,
                    x0,
                    currentRemainderType,
                    maxAnalysisOrder
                )
                
                // 显示图表
                displayChart(analysisData)
                
                // 转换为LaTeX并显示
                val latexExpression = TeXConverter.toTex(selectedFunction?.expression ?: "")
                val taylorLatex = remainderViewModel.getTaylorExpansionLatex(selectedFunction!!, result.x0, result.order)
                val remainderLatex = remainderViewModel.getRemainderLatex(selectedFunction!!, result.x, result.x0, result.order, currentRemainderType)
                
                mathFormulaViewer.displayFunctionAndRemainderFormula(
                    selectedFunction!!.name,
                    latexExpression,
                    taylorLatex,
                    remainderLatex,
                    result.order,
                    currentRemainderType.displayName
                )
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("导数阶数不足") == true -> "导数阶数不足：当前只支持最多8阶泰勒展开"
                    else -> "计算错误: ${e.message}"
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun displayResults(result: RemainderResult) {
        binding.textExactValue.text = "精确值: ${decimalFormat.format(result.exactValue)}"
        binding.textApproximateValue.text = "近似值: ${decimalFormat.format(result.approximateValue)}"
        binding.textActualError.text = "实际误差: ${decimalFormat.format(result.actualError)}"
        binding.textRemainderValue.text = "余项值: ${decimalFormat.format(result.remainderValue)}"
        
        // 生成LaTeX格式的泰勒展开式和余项公式
        val latexExpression = TeXConverter.toTex(selectedFunction?.expression ?: "")
        val taylorLatex = remainderViewModel.getTaylorExpansionLatex(selectedFunction!!, result.x0, result.order)
        val remainderLatex = remainderViewModel.getRemainderLatex(selectedFunction!!, result.x, result.x0, result.order, currentRemainderType)
        
        mathFormulaViewer.displayFunctionAndRemainderFormula(
            selectedFunction!!.name,
            latexExpression,
            taylorLatex,
            remainderLatex,
            result.order,
            currentRemainderType.displayName
        )
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
    
    private fun displayChart(analysisData: List<Pair<Int, Double>>) {
        // 确保数据非空
        if (analysisData.isEmpty()) {
            Toast.makeText(context, "没有有效的余项数据可显示", Toast.LENGTH_SHORT).show()
            return
        }
        
        val entries = analysisData.map { Entry(it.first.toFloat(), it.second.toFloat()) }
        
        val dataSet = LineDataSet(entries, "${currentRemainderType.displayName}余项")
        dataSet.color = requireContext().getColor(R.color.purple_500)
        dataSet.setCircleColor(requireContext().getColor(R.color.purple_500))
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 10f
        
        val lineData = LineData(dataSet)
        
        // 更详细的图表配置
        val chart = binding.chart
        chart.description.isEnabled = true
        chart.description.text = "余项与阶数关系"
        chart.setDrawGridBackground(false)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.xAxis.valueFormatter = IndexAxisValueFormatter()
        chart.axisLeft.setDrawGridLines(true)
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = true
        
        // 使用对数比例可以更清楚地看到余项随阶数增加的快速下降
        // chart.axisLeft.isLogarithmic = true
        
        chart.data = lineData
        chart.animateXY(1000, 1000)
        chart.invalidate()
        
        // 调试信息
        Log.d("RemainderFragment", "绘制余项图表，数据点数量: ${analysisData.size}")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}