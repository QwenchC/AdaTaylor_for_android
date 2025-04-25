package com.sqq.adataylor.core

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.PI

/**
 * 小波分析核心类
 * 实现不同种类的小波变换
 */
class WaveletCore {

    /**
     * 离散小波变换 (DWT)
     * @param signal 输入信号
     * @param waveletType 小波类型
     * @return 小波系数 (approximation和detail系数)
     */
    fun discreteWaveletTransform(signal: DoubleArray, waveletType: WaveletType): WaveletCoefficients {
        // 确保信号长度是2的幂
        if (!isPowerOfTwo(signal.size)) {
            throw IllegalArgumentException("信号长度必须是2的幂")
        }

        val n = signal.size
        val decompositionLevel = (Math.log(n.toDouble()) / Math.log(2.0)).toInt()
        
        // 获取小波滤波器
        val (lowPassFilter, highPassFilter) = getWaveletFilters(waveletType)
        
        // 执行多层小波分解
        return performDecomposition(signal, lowPassFilter, highPassFilter, decompositionLevel)
    }
    
    /**
     * 执行小波分解
     */
    private fun performDecomposition(
        signal: DoubleArray,
        lowPassFilter: DoubleArray,
        highPassFilter: DoubleArray,
        maxLevel: Int
    ): WaveletCoefficients {
        var currentSignal = signal.copyOf()
        val approximationCoefficients = mutableListOf<DoubleArray>()
        val detailCoefficients = mutableListOf<DoubleArray>()
        
        for (level in 1..maxLevel) {
            val n = currentSignal.size
            val halfN = n / 2
            
            val approximation = DoubleArray(halfN)
            val detail = DoubleArray(halfN)
            
            // 执行卷积和下采样
            for (i in 0 until halfN) {
                var approx = 0.0
                var det = 0.0
                
                for (j in 0 until lowPassFilter.size) {
                    val index = (2 * i + j) % n
                    approx += lowPassFilter[j] * currentSignal[index]
                    det += highPassFilter[j] * currentSignal[index]
                }
                
                approximation[i] = approx
                detail[i] = det
            }
            
            // 保存本级系数
            approximationCoefficients.add(approximation)
            detailCoefficients.add(detail)
            
            // 为下一级准备
            currentSignal = approximation
            
            // 如果已经分解到最低级，退出循环
            if (currentSignal.size <= 2) break
        }
        
        return WaveletCoefficients(approximationCoefficients, detailCoefficients)
    }
    
    /**
     * 小波重构
     * @param coefficients 小波系数
     * @param waveletType 小波类型
     * @return 重构后的信号
     */
    fun inverseWaveletTransform(coefficients: WaveletCoefficients, waveletType: WaveletType): DoubleArray {
        val (lowPassFilter, highPassFilter) = getWaveletFilters(waveletType)
        
        // 获取逆变换滤波器
        val inverseLowPass = getInverseFilter(lowPassFilter)
        val inverseHighPass = getInverseFilter(highPassFilter)
        
        // 从最低层开始重构
        var reconstructed = coefficients.approximation.last()
        
        for (level in coefficients.detail.indices.reversed()) {
            reconstructed = reconstructLevel(
                reconstructed,
                coefficients.detail[level],
                inverseLowPass,
                inverseHighPass
            )
        }
        
        return reconstructed
    }
    
    /**
     * 重构单一层级
     */
    private fun reconstructLevel(
        approximation: DoubleArray,
        detail: DoubleArray,
        inverseLowPass: DoubleArray,
        inverseHighPass: DoubleArray
    ): DoubleArray {
        val n = approximation.size
        val reconstructedSize = 2 * n
        val reconstructed = DoubleArray(reconstructedSize)
        
        // 上采样和卷积
        for (i in 0 until reconstructedSize) {
            var sum = 0.0
            
            for (j in 0 until inverseLowPass.size) {
                val approxIndex = (i/2 - j + n) % n
                if (i % 2 == 0 && approxIndex >= 0 && approxIndex < n) {
                    sum += inverseLowPass[j] * approximation[approxIndex]
                }
                
                val detailIndex = (i/2 - j + n) % n
                if (i % 2 == 1 && detailIndex >= 0 && detailIndex < n) {
                    sum += inverseHighPass[j] * detail[detailIndex]
                }
            }
            
            reconstructed[i] = sum
        }
        
        return reconstructed
    }
    
    /**
     * 获取逆变换滤波器
     */
    private fun getInverseFilter(filter: DoubleArray): DoubleArray {
        val inverse = DoubleArray(filter.size)
        for (i in filter.indices) {
            inverse[i] = filter[filter.size - 1 - i] * (if (i % 2 == 0) 1 else -1)
        }
        return inverse
    }
    
    /**
     * 获取小波滤波器
     */
    private fun getWaveletFilters(waveletType: WaveletType): Pair<DoubleArray, DoubleArray> {
        return when (waveletType) {
            WaveletType.HAAR -> {
                val lowPass = doubleArrayOf(1.0/sqrt(2.0), 1.0/sqrt(2.0))
                val highPass = doubleArrayOf(1.0/sqrt(2.0), -1.0/sqrt(2.0))
                Pair(lowPass, highPass)
            }
            WaveletType.DAUBECHIES4 -> {
                val h0 = (1.0 + sqrt(3.0)) / (4.0 * sqrt(2.0))
                val h1 = (3.0 + sqrt(3.0)) / (4.0 * sqrt(2.0))
                val h2 = (3.0 - sqrt(3.0)) / (4.0 * sqrt(2.0))
                val h3 = (1.0 - sqrt(3.0)) / (4.0 * sqrt(2.0))
                
                val lowPass = doubleArrayOf(h0, h1, h2, h3)
                val highPass = doubleArrayOf(h3, -h2, h1, -h0)
                Pair(lowPass, highPass)
            }
            WaveletType.MEXICAN_HAT -> {
                // 墨西哥帽小波是连续小波，这里提供一个离散化的近似
                val factor = 2.0 / sqrt(3.0 * sqrt(PI))
                val lowPass = DoubleArray(8) { i ->
                    val t = (i - 3.5) / 3.0
                    factor * (1.0 - t*t) * Math.exp(-t*t/2)
                }
                val highPass = DoubleArray(8) { i ->
                    val t = (i - 3.5) / 3.0
                    -factor * t * Math.exp(-t*t/2)
                }
                Pair(lowPass, highPass)
            }
        }
    }
    
    /**
     * 检查是否为2的幂
     */
    private fun isPowerOfTwo(n: Int): Boolean {
        return n > 0 && (n and (n - 1)) == 0
    }
    
    /**
     * 生成信号采样点
     * @param signalType 信号类型
     * @param size 采样点数量
     * @return 采样点数组
     */
    fun generateSignal(signalType: SignalType, size: Int): DoubleArray {
        val signal = DoubleArray(size)
        
        when (signalType) {
            SignalType.SINE -> {
                for (i in 0 until size) {
                    signal[i] = sin(2.0 * PI * i / size)
                }
            }
            SignalType.COSINE -> {
                for (i in 0 until size) {
                    signal[i] = cos(2.0 * PI * i / size)
                }
            }
            SignalType.SQUARE -> {
                for (i in 0 until size) {
                    signal[i] = if (i < size / 2) 1.0 else -1.0
                }
            }
            SignalType.SAWTOOTH -> {
                for (i in 0 until size) {
                    signal[i] = 2.0 * (i.toDouble() / size) - 1.0
                }
            }
            SignalType.CHIRP -> {
                for (i in 0 until size) {
                    val t = i.toDouble() / size
                    signal[i] = sin(2.0 * PI * 10 * t * t)
                }
            }
        }
        
        return signal
    }
}

/**
 * 小波类型枚举
 */
enum class WaveletType {
    HAAR,
    DAUBECHIES4,
    MEXICAN_HAT
}

/**
 * 信号类型枚举
 */
enum class SignalType {
    SINE,
    COSINE,
    SQUARE,
    SAWTOOTH,
    CHIRP
}

/**
 * 小波系数数据类
 */
data class WaveletCoefficients(
    val approximation: List<DoubleArray>,
    val detail: List<DoubleArray>
)