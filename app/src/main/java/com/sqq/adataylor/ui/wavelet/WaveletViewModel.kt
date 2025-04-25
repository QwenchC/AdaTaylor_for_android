package com.sqq.adataylor.ui.wavelet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sqq.adataylor.core.SignalType
import com.sqq.adataylor.core.WaveletCore
import com.sqq.adataylor.core.WaveletCoefficients
import com.sqq.adataylor.core.WaveletType
import kotlin.math.sqrt

class WaveletViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "小波分析"
    }
    val text: LiveData<String> = _text

    private val waveletCore = WaveletCore()
    
    // 生成信号数据
    fun generateSignal(signalType: SignalType, size: Int): DoubleArray {
        return waveletCore.generateSignal(signalType, size)
    }
    
    // 执行小波分析
    fun performWaveletAnalysis(signal: DoubleArray, waveletType: WaveletType): WaveletCoefficients {
        return waveletCore.discreteWaveletTransform(signal, waveletType)
    }
    
    // 执行信号重构
    fun reconstructSignal(coefficients: WaveletCoefficients, waveletType: WaveletType): DoubleArray {
        return waveletCore.inverseWaveletTransform(coefficients, waveletType)
    }
    
    // 计算重构误差
    fun calculateReconstructionError(original: DoubleArray, reconstructed: DoubleArray): Double {
        if (original.size != reconstructed.size) return Double.NaN
        
        var sumSquaredError = 0.0
        for (i in original.indices) {
            val error = original[i] - reconstructed[i]
            sumSquaredError += error * error
        }
        
        return sqrt(sumSquaredError / original.size)
    }
}