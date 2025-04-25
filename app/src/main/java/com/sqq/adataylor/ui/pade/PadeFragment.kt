package com.sqq.adataylor.ui.pade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sqq.adataylor.data.DataPoint
import com.sqq.adataylor.data.FunctionModel
import com.sqq.adataylor.databinding.FragmentPadeBinding
import java.text.DecimalFormat

class PadeFragment : Fragment() {

    private var _binding: FragmentPadeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var padeViewModel: PadeViewModel
    private var selectedFunction: FunctionModel? = null
    private var taylorOrder = 5
    private var padeM = 2
    private var padeN = 3
    
    private val decimalFormat = DecimalFormat("#.######")
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        padeViewModel = ViewModelProvider(this).get(PadeViewModel::class.java)
        
        _binding = FragmentPadeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        // 设置标题
        padeViewModel.text.observe(viewLifecycleOwner) {
            binding.textPadeTitle.text = it
        }
        
        setupFunctionSpinner()
        setupTaylorOrderSeekbar()
        setupPadeParametersSeekbars()
        setupCalculateButton()
        
        return root
    }
    
    private fun setupFunctionSpinner() {
        val functions = padeViewModel.predefinedFunctions
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
                selectedFunction = functions[position]
                binding.textFunctionExpression.text = "f(x) = ${selectedFunction?.expression ?: ""}"
                
                // 设置计算范围默认值
                val domain = selectedFunction?.domain ?: Pair(-5.0, 5.0)
                binding.editRangeStart.setText(domain.first.toString())
                binding.editRangeEnd.setText(domain.second.toString())
                binding.editX0.setText(selectedFunction?.defaultX0.toString())
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
    
    private fun setupTaylorOrderSeekbar() {
        binding.seekbarTaylorOrder.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                taylorOrder = progress
                binding.textTaylorOrder.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupPadeParametersSeekbars() {
        binding.seekbarPadeM.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                padeM = progress
                binding.textPadeM.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        binding.seekbarPadeN.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                padeN = progress
                binding.textPadeN.text = progress.toString()
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
                val x0 = binding.editX0.text.toString().toDouble()
                val rangeStart = binding.editRangeStart.text.toString().toDouble()
                val rangeEnd = binding.editRangeEnd.text.toString().toDouble()
                
                if (rangeStart >= rangeEnd) {
                    Toast.makeText(context, "起始点必须小于结束点", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                // 执行对比计算
                val (exactPoints, taylorPoints, padePoints) = padeViewModel.generateComparisonData(
                    function = selectedFunction!!,
                    start = rangeStart,
                    end = rangeEnd,
                    x0 = x0,
                    taylorOrder = taylorOrder,
                    padeM = padeM,
                    padeN = padeN
                )
                
                // 显示函数逼近对比
                binding.chartApproximation.setData(
                    exactPoints = exactPoints,
                    approximatePoints = taylorPoints,
                    thirdPoints = padePoints,
                    exactLabel = "精确值",
                    approximateLabel = "泰勒展开",
                    thirdLabel = "帕德逼近"
                )
                
                // 生成误差分析图表数据
                val taylorErrorPoints = mutableListOf<DataPoint>()
                val padeErrorPoints = mutableListOf<DataPoint>()
                
                for (i in taylorPoints.indices) {
                    if (i < exactPoints.size) {
                        val x = exactPoints[i].x
                        val exactY = exactPoints[i].y
                        
                        // 泰勒误差
                        if (i < taylorPoints.size) {
                            val taylorY = taylorPoints[i].y
                            val taylorError = Math.abs(exactY - taylorY)
                            taylorErrorPoints.add(DataPoint(x, taylorError))
                        }
                        
                        // 帕德误差
                        if (i < padePoints.size) {
                            val padeY = padePoints[i].y
                            if (!padeY.isNaN()) {
                                val padeError = Math.abs(exactY - padeY)
                                padeErrorPoints.add(DataPoint(x, padeError))
                            }
                        }
                    }
                }
                
                // 显示误差分析
                binding.chartError.setData(
                    exactPoints = emptyList(),
                    approximatePoints = taylorErrorPoints,
                    thirdPoints = padeErrorPoints,
                    approximateLabel = "泰勒误差",
                    thirdLabel = "帕德误差"
                )
                
                // 验证定理6.16
                verifyTheorem(x0)
                
            } catch (e: Exception) {
                Toast.makeText(context, "计算出错: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun verifyTheorem(x0: Double) {
        val testPoints = listOf(
            x0 + 0.5,
            x0 + 1.0,
            x0 + 1.5,
            x0 + 2.0,
            x0 + 3.0
        )
        
        // 计算误差分析
        val errorData = padeViewModel.calculateErrorAnalysis(
            function = selectedFunction!!,
            x0 = x0,
            testPoints = testPoints,
            taylorOrder = taylorOrder,
            padeM = padeM,
            padeN = padeN
        )
        
        // 更新误差表格
        val tableRows = listOf(
            binding.row1,
            binding.row2,
            binding.row3,
            binding.row4,
            binding.row5
        )
        
        // 清空表格
        for (row in tableRows) {
            row.removeAllViews()
        }
        
        // 填充表格
        for (i in errorData.indices) {
            if (i >= tableRows.size) break
            
            val row = tableRows[i]
            val (distance, taylorError, padeError) = errorData[i]
            
            // 距离
            val distanceTextView = TextView(context)
            distanceTextView.text = decimalFormat.format(distance)
            distanceTextView.setPadding(8, 8, 8, 8)
            row.addView(distanceTextView)
            
            // 泰勒误差
            val taylorErrorTextView = TextView(context)
            taylorErrorTextView.text = decimalFormat.format(taylorError)
            taylorErrorTextView.setPadding(8, 8, 8, 8)
            row.addView(taylorErrorTextView)
            
            // 帕德误差
            val padeErrorTextView = TextView(context)
            padeErrorTextView.text = if (padeError.isNaN()) "-" else decimalFormat.format(padeError)
            padeErrorTextView.setPadding(8, 8, 8, 8)
            row.addView(padeErrorTextView)
            
            // 误差比
            val ratioTextView = TextView(context)
            val ratio = if (padeError.isNaN() || padeError == 0.0) Double.NaN else taylorError / padeError
            ratioTextView.text = if (ratio.isNaN()) "-" else decimalFormat.format(ratio)
            ratioTextView.setPadding(8, 8, 8, 8)
            row.addView(ratioTextView)
        }
        
        // 更新结论
        val theoreticalPower = "O(|x - x0|^$padeM)"
        binding.textConclusion.text = "结论：泰勒-帕德误差比满足 $theoreticalPower，" +
                "对于有理函数特性的函数，当距离展开点较远时，帕德逼近通常比泰勒展开更精确。"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}