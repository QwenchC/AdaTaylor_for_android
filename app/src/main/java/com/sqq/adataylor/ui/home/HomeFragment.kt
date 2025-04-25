package com.sqq.adataylor.ui.home

import android.os.Bundle
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
import com.sqq.adataylor.data.FunctionModel
import com.sqq.adataylor.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private var selectedFunction: FunctionModel? = null
    private var currentOrder = 3

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

    private fun setupCalculateButton() {
        binding.buttonCalculate.setOnClickListener {
            val x = binding.editX.text.toString().toDoubleOrNull()
            val x0 = binding.editX0.text.toString().toDoubleOrNull()

            if (x == null || x0 == null || selectedFunction == null) {
                Toast.makeText(context, "请输入有效的x和x0值", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = homeViewModel.calculateTaylor(selectedFunction!!, x, x0, currentOrder)
            
            binding.textResultExact.text = "精确值: ${result.exactValue}"
            binding.textResultApproximate.text = "近似值: ${result.approximateValue}"
            binding.textResultError.text = "误差: ${result.error}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}