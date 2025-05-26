package com.example.monay.data

/**
 * 类别金额数据类，用于存储按类别统计的金额
 */
data class CategoryAmount(
    val category: String,
    val amount: Double
)

/**
 * 类型金额数据类，用于存储按类型（收入/支出）统计的金额
 */
data class TypeAmount(
    val type: String,
    val amount: Double
)

/**
 * 时间范围数据类，用于表示统计的时间范围
 */
data class TimeRange(
    val startTime: Long,
    val endTime: Long
)

/**
 * 统计时间单位枚举
 */
enum class StatisticTimeUnit {
    MONTH,
    YEAR
} 