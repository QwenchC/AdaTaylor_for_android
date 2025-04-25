package com.sqq.adataylor.ui.home

import android.app.AlertDialog
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
import com.sqq.adataylor.data.CustomFunctionHelper
import com.sqq.adataylor.data.FunctionManager
import com.sqq.adataylor.data.FunctionModel
import com.sqq.adataylor.data.TaylorResult
import com.sqq.adataylor.databinding.DialogCustomFunctionBinding
import com.sqq.adataylor.databinding.FragmentHomeBinding
import java.text.DecimalFormat

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private var selectedFunction: FunctionModel? = null
    private var currentOrder = 3
    private val decimalFormat = DecimalFormat("#.########")
    private val customFunctionHelper = CustomFunctionHelper()
    private var customFunction: FunctionModel? = null

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
        setupCustomFunctionButton() // 添加自定义函数按钮设置

        return root
    }

    // 添加自定义函数按钮处理
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
            // 设置到函数管理器，确保其他页面也能访问
            FunctionManager.setCustomFunction(function)
            customFunction = function
            
            // 重新设置Spinner适配器
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
                    binding.editX0.setText(selectedFunction?.defaultX0.toString())
                }
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
    
    // 修改displayResult方法，确保显示函数表达式
    private fun displayResult(result: TaylorResult) {
        // 确保显示函数表达式
        binding.textResultFunction.text = "函数: ${selectedFunction?.expression ?: "未知函数"}"
        binding.textResultPoints.text = "计算点: x=${result.x}, 展开点: x0=${result.x0}"
        binding.textResultOrder.text = "展开阶数: ${result.order}"
        
        binding.textResultExact.text = "精确值: ${decimalFormat.format(result.exactValue)}"
        binding.textResultApproximate.text = "近似值: ${decimalFormat.format(result.approximateValue)}"
        binding.textResultError.text = "误差: ${decimalFormat.format(result.error)}"
        binding.textResultErrorEstimate.text = "误差估计: ${decimalFormat.format(result.errorEstimate)}"
        
        // 添加泰勒展开式显示
        val taylorExpansionText = homeViewModel.getTaylorExpansionText(
            selectedFunction!!, result.x0, result.order
        )
        binding.textTaylorExpansion.text = "泰勒展开式: $taylorExpansionText"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}