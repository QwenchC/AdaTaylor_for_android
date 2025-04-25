package com.sqq.adataylor.ui.slideshow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqq.adataylor.data.TaylorExample

class SlideshowViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Taylor展开应用示例"
    }
    val text: LiveData<String> = _text
    
    // 预定义一些示例
    val examples = listOf(
        TaylorExample(
            "计算sin(0.1)的近似值",
            "使用sin(x)在x=0处的Taylor展开来近似计算sin(0.1)",
            "sin(x) ≈ x - x³/3! + x⁵/5! - ...",
            "对于x=0.1, 使用3阶Taylor展开:\nsin(0.1) ≈ 0.1 - (0.1)³/6 = 0.1 - 0.000167 = 0.099833\n实际值: sin(0.1) = 0.099833"
        ),
        TaylorExample(
            "自然对数ln(1.1)的计算",
            "使用ln(1+x)在x=0处的Taylor展开来近似计算ln(1.1)",
            "ln(1+x) ≈ x - x²/2 + x³/3 - x⁴/4 + ...",
            "对于x=0.1, 使用4阶Taylor展开:\nln(1.1) ≈ 0.1 - (0.1)²/2 + (0.1)³/3 - (0.1)⁴/4\n = 0.1 - 0.005 + 0.00033 - 0.000025 = 0.095305\n实际值: ln(1.1) = 0.09531"
        ),
        TaylorExample(
            "余弦函数cos(0.2)的近似值",
            "使用cos(x)在x=0处的Taylor展开来近似计算cos(0.2)",
            "cos(x) ≈ 1 - x²/2! + x⁴/4! - ...",
            "对于x=0.2, 使用4阶Taylor展开:\ncos(0.2) ≈ 1 - (0.2)²/2 + (0.2)⁴/24\n = 1 - 0.02 + 0.000067 = 0.980067\n实际值: cos(0.2) = 0.980067"
        ),
        TaylorExample(
            "误差分析与阶数选择",
            "分析不同阶数Taylor展开式的精度和误差",
            "e^x 在 x=0 的展开式: e^x ≈ 1 + x + x²/2! + x³/3! + ...",
            "计算e^0.5的例子:\n1阶: 1 + 0.5 = 1.5, 误差: 0.14873\n2阶: 1 + 0.5 + 0.5²/2 = 1.625, 误差: 0.02373\n3阶: 1 + 0.5 + 0.5²/2 + 0.5³/6 = 1.6458, 误差: 0.00293\n实际值: e^0.5 = 1.64872"
        )
    )
}