package com.sqq.adataylor.ui.hybrid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sqq.adataylor.core.WaveletType
import com.sqq.adataylor.data.DataPoint
import com.sqq.adataylor.data.FunctionManager
import com.sqq.adataylor.data.FunctionModel
import com.sqq.adataylor.databinding.FragmentHybridBinding
import java.text.DecimalFormat

class HybridFragment : Fragment() {

    private var _binding: FragmentHybridBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var hybridViewModel: HybridViewModel
    private var selectedFunction: FunctionModel? = null
    private var selectedWaveletType = WaveletType.HAAR
    private var waveletLevel = 3
    private var taylorOrder1 = 3
    private var taylorOrder2 = 3
    
    private val decimalFormat = DecimalFormat("#.########")
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hybridViewModel = ViewModelProvider(this).get(HybridViewModel::class.java)
        
        _binding = FragmentHybridBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        // 设置标题
        hybridViewModel.text.observe(viewLifecycleOwner) {
            binding.textHybridTitle.text = it
        }
        
        setupFunctionSpinner()
        setupWaveletTypeSpinner()
        setupTaylorOrderSeekbars()
        setupWaveletLevelSeekbar()
        setupCalculateButton()
        setupCompareButton()
        
        return root
    }
    
    private fun setupFunctionSpinner() {
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
                selectedFunction = functions[position]
                binding.textFunctionExpression.text = "f(x) = ${selectedFunction?.expression ?: ""}"
                
                // 设置区间默认值
                val domain = selectedFunction?.domain ?: Pair(-5.0, 5.0)
                binding.editIntervalStart.setText(domain.first.toString())
                binding.editIntervalEnd.setText(domain.second.toString())
                binding.editIntervalMiddle.setText("0.0")
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
    
    private fun setupWaveletTypeSpinner() {
        val waveletTypes = WaveletType.values().map { it.name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            waveletTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.spinnerWaveletType.adapter = adapter
        binding.spinnerWaveletType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedWaveletType = WaveletType.values()[position]
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
    
    private fun setupTaylorOrderSeekbars() {
        binding.seekbarTaylorOrder1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                taylorOrder1 = progress
                binding.textTaylorOrder1.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        binding.seekbarTaylorOrder2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                taylorOrder2 = progress
                binding.textTaylorOrder2.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupWaveletLevelSeekbar() {
        binding.seekbarWaveletLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveletLevel = progress
                binding.textWaveletLevel.text = progress.toString()
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
                val start = binding.editIntervalStart.text.toString().toDouble()
                val middle = binding.editIntervalMiddle.text.toString().toDouble()
                val end = binding.editIntervalEnd.text.toString().toDouble()
                
                if (start >= middle || middle >= end) {
                    Toast.makeText(context, "请确保区间满足: 起始点 < 中间点 < 结束点", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                // 执行混合逼近
                val intervals = listOf(start, middle, end)
                val taylorOrders = listOf(taylorOrder1, taylorOrder2)
                val taylorPoints = listOf(
                    (start + middle) / 2,
                    (middle + end) / 2
                )
                
                val (exactPoints, approximatePoints, meanError) = hybridViewModel.performMixedApproximation(
                    function = selectedFunction!!,
                    intervals = intervals,
                    taylorOrders = taylorOrders,
                    taylorExpansionPoints = taylorPoints,
                    waveletType = selectedWaveletType,
                    waveletLevels = waveletLevel
                )
                
                // 显示结果
                binding.chartHybrid.setData(
                    exactPoints = exactPoints,
                    approximatePoints = approximatePoints,
                    exactLabel = "精确值",
                    approximateLabel = "混合逼近"
                )
                
                binding.textError.text = "均方误差: ${decimalFormat.format(meanError)}"
                
            } catch (e: Exception) {
                Toast.makeText(context, "计算出错: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupCompareButton() {
        binding.buttonCompare.setOnClickListener {
            if (selectedFunction == null) {
                Toast.makeText(context, "请先选择一个函数", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            try {
                val start = binding.editIntervalStart.text.toString().toDouble()
                val middle = binding.editIntervalMiddle.text.toString().toDouble()
                val end = binding.editIntervalEnd.text.toString().toDouble()
                
                val intervals = listOf(start, middle, end)
                val taylorOrders = listOf(taylorOrder1, taylorOrder2)
                val taylorPoints = listOf(
                    (start + middle) / 2,
                    (middle + end) / 2
                )
                
                // 执行比较
                val taylorExpansionPoint = (start + end) / 2
                val errors = hybridViewModel.compareApproximationMethods(
                    function = selectedFunction!!,
                    x0 = taylorExpansionPoint,
                    taylorOrder = (taylorOrder1 + taylorOrder2) / 2, // 使用平均阶数
                    waveletType = selectedWaveletType,
                    waveletLevels = waveletLevel,
                    mixedIntervals = intervals,
                    mixedTaylorOrders = taylorOrders,
                    mixedTaylorPoints = taylorPoints
                )
                
                // 显示比较结果
                binding.textErrorTaylor.text = decimalFormat.format(errors["Taylor"] ?: Double.NaN)
                binding.textErrorWavelet.text = decimalFormat.format(errors["Wavelet"] ?: Double.NaN)
                binding.textErrorHybrid.text = decimalFormat.format(errors["Mixed"] ?: Double.NaN)
                
            } catch (e: Exception) {
                Toast.makeText(context, "比较出错: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}