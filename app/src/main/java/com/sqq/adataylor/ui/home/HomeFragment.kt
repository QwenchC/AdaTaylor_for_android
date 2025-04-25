package com.sqq.adataylor.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sqq.adataylor.data.FunctionModel
import com.sqq.adataylor.data.TaylorResult
import com.sqq.adataylor.databinding.FragmentHomeBinding
import java.text.DecimalFormat

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private var selectedFunction: FunctionModel? = null
    private var currentOrder = 3
    private val decimalFormat = DecimalFormat("#.########")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        setupFunctionSpinner()
        setupOrderSeekBar()
        setupAdaptiveSwitch()
        setupCalculateButton()

        return root
    }

    private fun setupFunctionSpinner() {
        val functions = homeViewModel.predefinedFunctions
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
                binding.editX0.setText(selectedFunction?.defaultX0.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedFunction = null
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
    
    private fun setupAdaptiveSwitch() {
        binding.switchAdaptive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.layoutManualOrder.visibility = View.GONE
                binding.layoutAdaptive.visibility = View.VISIBLE
            } else {
                binding.layoutManualOrder.visibility = View.VISIBLE
                binding.layoutAdaptive.visibility = View.GONE
            }
        }
    }

    private fun setupCalculateButton() {
        binding.buttonCalculate.setOnClickListener {
            val x = binding.editX.text.toString().toDoubleOrNull()
            val x0 = binding.editX0.text.toString().toDoubleOrNull()

            if (x == null || x0 == null || selectedFunction == null) {
                Toast.makeText(context, "请输入有效的x和x0值", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = if (binding.switchAdaptive.isChecked) {
                val targetError = binding.editError.text.toString().toDoubleOrNull() ?: 0.0001
                homeViewModel.calculateAdaptiveTaylor(selectedFunction!!, x, x0, targetError)
            } else {
                homeViewModel.calculateTaylor(selectedFunction!!, x, x0, currentOrder)
            }
            
            displayResult(result)
        }
    }
    
    private fun displayResult(result: TaylorResult) {
        binding.textResultFunction.text = "函数: ${selectedFunction?.expression}"
        binding.textResultPoints.text = "计算点: x=${result.x}, 展开点: x0=${result.x0}"
        binding.textResultOrder.text = "展开阶数: ${result.order}"
        binding.textResultExact.text = "精确值: ${decimalFormat.format(result.exactValue)}"
        binding.textResultApproximate.text = "近似值: ${decimalFormat.format(result.approximateValue)}"
        binding.textResultError.text = "实际误差: ${decimalFormat.format(result.error)}"
        binding.textResultErrorEstimate.text = "误差估计: ${decimalFormat.format(result.errorEstimate)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}