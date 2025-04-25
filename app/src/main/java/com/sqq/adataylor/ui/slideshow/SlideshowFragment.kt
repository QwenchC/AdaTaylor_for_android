package com.sqq.adataylor.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sqq.adataylor.R
import com.sqq.adataylor.data.TaylorExample
import com.sqq.adataylor.databinding.FragmentSlideshowBinding
import com.sqq.adataylor.databinding.ItemExampleBinding

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSlideshow
        slideshowViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        
        // 设置RecyclerView
        binding.recyclerExamples.layoutManager = LinearLayoutManager(context)
        binding.recyclerExamples.adapter = ExampleAdapter(slideshowViewModel.examples)
        
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    // 示例适配器
    private inner class ExampleAdapter(private val examples: List<TaylorExample>) :
        RecyclerView.Adapter<ExampleAdapter.ExampleViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
            val binding = ItemExampleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ExampleViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
            holder.bind(examples[position])
        }
        
        override fun getItemCount() = examples.size
        
        inner class ExampleViewHolder(private val binding: ItemExampleBinding) :
            RecyclerView.ViewHolder(binding.root) {
            
            fun bind(example: TaylorExample) {
                binding.textExampleTitle.text = example.title
                binding.textExampleDescription.text = example.description
                binding.textExampleFormula.text = example.formula
                binding.textExampleCalculation.text = example.calculation
                
                // 如果有关联函数，添加尝试计算按钮
                if (example.function != null) {
                    binding.buttonTryCalculate.visibility = View.VISIBLE
                    binding.buttonTryCalculate.setOnClickListener {
                        navigateToCalculator(example)
                    }
                } else {
                    binding.buttonTryCalculate.visibility = View.GONE
                }
            }
            
            private fun navigateToCalculator(example: TaylorExample) {
                // 转到Home计算器界面
                Toast.makeText(
                    context, 
                    "请前往计算器页面使用${example.function?.name}进行计算", 
                    Toast.LENGTH_SHORT
                ).show()
                
                // 可以考虑使用Navigation组件导航
                findNavController().navigate(R.id.nav_home)
            }
        }
    }
}