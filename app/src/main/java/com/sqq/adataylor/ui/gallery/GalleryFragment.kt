package com.sqq.adataylor.ui.gallery

import android.graphics.Color
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
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sqq.adataylor.data.FunctionModel
import com.sqq.adataylor.databinding.FragmentGalleryBinding
import com.sqq.adataylor.ui.home.HomeViewModel

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var homeViewModel: HomeViewModel
    private var selectedFunction: FunctionModel? = null
    private var currentOrder = 3

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
        setupChart()

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

    private fun plotFunction(start: Double, end: Double, x0: Double) {
        val (exactPoints, taylorPoints) = galleryViewModel.generateFunctionPoints(
            selectedFunction!!,
            start,
            end,
            x0,
            currentOrder
        )
        
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