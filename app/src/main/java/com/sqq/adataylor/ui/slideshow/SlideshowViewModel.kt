package com.sqq.adataylor.ui.slideshow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqq.adataylor.data.PredefinedFunctions
import com.sqq.adataylor.data.TaylorExample

class SlideshowViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Taylor展开应用示例"
    }
    val text: LiveData<String> = _text
    
    // 预定义更多示例
    val examples = listOf(
        TaylorExample(
            "计算sin(0.1)的近似值",
            "使用sin(x)在x=0处的Taylor展开来近似计算sin(0.1)",
            "sin(x) ≈ x - x³/3! + x⁵/5! - ...",
            "对于x=0.1, 使用3阶Taylor展开:\nsin(0.1) ≈ 0.1 - (0.1)³/6 = 0.1 - 0.000167 = 0.099833\n实际值: sin(0.1) = 0.099833",
            function = PredefinedFunctions.sine
        ),
        TaylorExample(
            "自然对数ln(1.1)的计算",
            "使用ln(1+x)在x=0处的Taylor展开来近似计算ln(1.1)",
            "ln(1+x) ≈ x - x²/2 + x³/3 - x⁴/4 + ...",
            "对于x=0.1, 使用4阶Taylor展开:\nln(1.1) ≈ 0.1 - (0.1)²/2 + (0.1)³/3 - (0.1)⁴/4\n = 0.1 - 0.005 + 0.00033 - 0.000025 = 0.095305\n实际值: ln(1.1) = 0.09531",
            function = PredefinedFunctions.logarithm
        ),
        TaylorExample(
            "余弦函数cos(0.2)的近似值",
            "使用cos(x)在x=0处的Taylor展开来近似计算cos(0.2)",
            "cos(x) ≈ 1 - x²/2! + x⁴/4! - ...",
            "对于x=0.2, 使用4阶Taylor展开:\ncos(0.2) ≈ 1 - (0.2)²/2 + (0.2)⁴/24\n = 1 - 0.02 + 0.000067 = 0.980067\n实际值: cos(0.2) = 0.980067",
            function = PredefinedFunctions.cosine
        ),
        TaylorExample(
            "误差分析与阶数选择",
            "分析不同阶数Taylor展开式的精度和误差",
            "e^x 在 x=0 的展开式: e^x ≈ 1 + x + x²/2! + x³/3! + ...",
            "计算e^0.5的例子:\n1阶: 1 + 0.5 = 1.5, 误差: 0.14873\n2阶: 1 + 0.5 + 0.5²/2 = 1.625, 误差: 0.02373\n3阶: 1 + 0.5 + 0.5²/2 + 0.5³/6 = 1.6458, 误差: 0.00293\n实际值: e^0.5 = 1.64872",
            function = PredefinedFunctions.exponential
        ),
        TaylorExample(
            "自适应阶数选择",
            "如何根据目标误差自动选择合适的展开阶数",
            "自适应算法根据误差估计公式选择最小所需阶数",
            "例如计算sin(0.5)，目标误差为0.0001：\n1阶泰勒展开误差估计为cos(0)|0.5|²/2 = 0.125\n2阶泰勒展开误差估计为sin(0)|0.5|³/6 = 0.0208\n3阶泰勒展开误差估计为cos(0)|0.5|⁴/24 = 0.0026\n4阶泰勒展开误差估计为sin(0)|0.5|⁵/120 = 0.0002\n5阶泰勒展开误差估计为cos(0)|0.5|⁶/720 = 0.00002 < 0.0001\n因此选择5阶展开",
            function = PredefinedFunctions.sine
        ),
        TaylorExample(
            "多变量函数的Taylor展开",
            "多变量函数可以使用偏导数进行Taylor展开",
            "f(x,y) ≈ f(a,b) + fx(a,b)(x-a) + fy(a,b)(y-b) + 高阶项...",
            "例如函数f(x,y) = e^(x+y)在(0,0)处的展开：\nf(x,y) ≈ 1 + x + y + (x²+2xy+y²)/2 + ...",
            function = null
        ),
        TaylorExample(
            "复合函数的Taylor展开",
            "通过链式法则计算复合函数的导数",
            "如果h(x) = f(g(x))，则h'(x) = f'(g(x))·g'(x)",
            "例如h(x) = sin(x²)的展开，可以写成h(x) = sin(u)，u = x²\n在x=0处展开：h(0) = 0\nh'(0) = cos(0)·2x|₍ₓ₌₀₎ = 0\nh''(0) = -sin(0)·(2x)² + cos(0)·2|₍ₓ₌₀₎ = 2\n因此，sin(x²) ≈ x² - x⁶/3! + ...",
            function = null
        ),
        TaylorExample(
            "物理学中的应用",
            "Taylor展开在物理学中广泛应用于近似计算",
            "例如：运动方程、场论、热力学等",
            "物体以初速度v₀从高处下落时，其位移可表示为：\ns(t) = s₀ + v₀t - gt²/2 + ...\n这实际上是位置函数s(t)关于时间t的二阶Taylor展开",
            function = null
        )
    )
}