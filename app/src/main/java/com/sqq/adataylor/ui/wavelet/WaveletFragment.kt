package com.sqq.adataylor.ui.wavelet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sqq.adataylor.core.SignalType
import com.sqq.adataylor.core.WaveletCoefficients
import com.sqq.adataylor.core.WaveletType
import com.sqq.adataylor.databinding.FragmentWaveletBinding
import com.sqq.adataylor.databinding.ItemWaveletLevelBinding
import java.text.DecimalFormat

class WaveletFragment : Fragment() {

    private var _binding: FragmentWaveletBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var waveletViewModel: WaveletViewModel
    
    private var selectedSignalType = SignalType.SINE
    private var selectedWaveletType = WaveletType.HAAR
    private var selectedSampleSize = 128
    
    private var originalSignal: DoubleArray = doubleArrayOf()
    private var waveletCoefficients: WaveletCoefficients? = null
    private var reconstructedSignal: DoubleArray = doubleArrayOf()
    
    private val decimalFormat = DecimalFormat("#.######")
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        waveletViewModel = ViewModelProvider(this).get(WaveletViewModel::class.java)
        
        _binding = FragmentWaveletBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        setupSignalTypeSpinner()
        setupWaveletTypeSpinner()
        setupSampleSizeRadioGroup()
        setupAnalyzeButton()
        setupRecyclerView()
        
        return root
    }
    
    private fun setupSignalTypeSpinner() {
        val signalTypes = SignalType.values().map { it.name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            signalTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.spinnerSignalType.adapter = adapter
        binding.spinnerSignalType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedSignalType = SignalType.values()[position]
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
    
    private fun setupSampleSizeRadioGroup() {
        binding.radioGroupSampleSize.setOnCheckedChangeListener { _, checkedId ->
            selectedSampleSize = when (checkedId) {
                binding.radioSize32.id -> 32
                binding.radioSize64.id -> 64
                binding.radioSize128.id -> 128
                binding.radioSize256.id -> 256
                else -> 128
            }
        }
    }
    
    private fun setupAnalyzeButton() {
        binding.buttonAnalyze.setOnClickListener {
            try {
                performWaveletAnalysis()
            } catch (e: Exception) {
                Toast.makeText(context, "分析失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupRecyclerView() {
        binding.recyclerWaveletLevels.layoutManager = LinearLayoutManager(context)
    }
    
    private fun performWaveletAnalysis() {
        // 生成原始信号
        originalSignal = waveletViewModel.generateSignal(selectedSignalType, selectedSampleSize)
        binding.chartOriginalSignal.setSignalData(originalSignal)
        
        // 执行小波变换
        waveletCoefficients = waveletViewModel.performWaveletAnalysis(originalSignal, selectedWaveletType)
        
        // 设置小波级别适配器
        waveletCoefficients?.let { coefficients ->
            binding.recyclerWaveletLevels.adapter = WaveletLevelAdapter(coefficients)
        }
        
        // 执行信号重构
        waveletCoefficients?.let { coefficients ->
            reconstructedSignal = waveletViewModel.reconstructSignal(coefficients, selectedWaveletType)
            binding.chartReconstructedSignal.setSignalData(reconstructedSignal)
            
            // 计算重构误差
            val error = waveletViewModel.calculateReconstructionError(originalSignal, reconstructedSignal)
            binding.textReconstructionError.text = "重构误差: ${decimalFormat.format(error)}"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    /**
     * 小波级别适配器
     */
    private inner class WaveletLevelAdapter(
        private val coefficients: WaveletCoefficients
    ) : RecyclerView.Adapter<WaveletLevelAdapter.WaveletLevelViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaveletLevelViewHolder {
            val binding = ItemWaveletLevelBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return WaveletLevelViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: WaveletLevelViewHolder, position: Int) {
            holder.bind(position)
        }
        
        override fun getItemCount(): Int = coefficients.approximation.size
        
        inner class WaveletLevelViewHolder(private val binding: ItemWaveletLevelBinding) : 
            RecyclerView.ViewHolder(binding.root) {
            
            fun bind(position: Int) {
                val level = position + 1
                binding.textLevelTitle.text = "级别 $level"
                
                // 设置近似系数和细节系数图表
                val approximation = coefficients.approximation[position]
                val detail = coefficients.detail[position]
                
                binding.chartApproximation.setSignalData(approximation)
                binding.chartDetail.setSignalData(detail)
            }
        }
    }
}