package com.sqq.adataylor.ui.remainder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sqq.adataylor.R
import com.sqq.adataylor.core.RemainderType
import com.sqq.adataylor.databinding.FragmentRemainderBinding
import com.sqq.adataylor.data.FunctionModel
import com.sqq.adataylor.data.FunctionManager  // 修改: FunctionRepository -> FunctionManager
import com.sqq.adataylor.util.MathFormulaViewer
import com.sqq.adataylor.util.TeXConverter
import java.text.DecimalFormat

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
    
    private fun setupOrderSlider() {
        binding.seekbarOrder.max = 10 // 限制最大阶数为10
        binding.seekbarOrder.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                currentOrder = progress
                binding.textOrder.text = "阶数: $progress"
            }
            
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }
    
    private fun setupCalculateButton() {
        binding.buttonCalculate.setOnClickListener {
            try {
                if (selectedFunction == null) {
                    Toast.makeText(context, "请先选择一个函数", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                val x0 = binding.editX0.text.toString().toDoubleOrNull()
                val x = binding.editX.text.toString().toDoubleOrNull()
                
                if (x0 == null || x == null) {
                    Toast.makeText(context, "请输入有效的展开点和计算点", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                // 添加阶数限制
                if (currentOrder > 10) {
                    Toast.makeText(context, "泰勒展开阶数过高，最多支持10阶", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                // 计算余项
                val remainderResult = remainderViewModel.calculateRemainder(
                    selectedFunction!!,
                    x,
                    x0,
                    currentOrder,
                    currentRemainderType
                )
                
                displayResults(remainderResult)
                
                // 分析不同阶数的余项
                val analysisData = remainderViewModel.analyzeRemainderByOrder(
                    selectedFunction!!,
                    x,
                    x0,
                    currentRemainderType
                )
                
                // 在图表中显示分析结果
                displayChart(analysisData)
            } catch (e: Exception) {
                // 提供更详细的错误信息
                val errorMessage = when {
                    e.message?.contains("导数阶数不足") == true -> "导数阶数不足：当前只支持最多10阶泰勒展开"
                    else -> "计算出错: ${e.message}"
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
        val entries = analysisData.map { Entry(it.first.toFloat(), it.second.toFloat()) }
        
        val dataSet = LineDataSet(entries, "${currentRemainderType.displayName}余项")
        dataSet.color = requireContext().getColor(R.color.purple_500) // 使用有效的颜色资源
        dataSet.setCircleColor(requireContext().getColor(R.color.purple_500)) // 使用有效的颜色资源
        dataSet.setDrawValues(false)
        
        val lineData = LineData(dataSet)
        binding.chart.data = lineData
        binding.chart.invalidate()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}