package com.example.monay.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monay.data.BillEntity
import com.example.monay.data.CategoryAmount
import com.example.monay.data.StatisticTimeUnit
import com.example.monay.data.TypeAmount
import com.example.monay.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * 统计数据状态类
 */
data class StatisticsState(
    val isLoading: Boolean = false,
    val expensesByCategory: List<CategoryAmount> = emptyList(),
    val incomesByCategory: List<CategoryAmount> = emptyList(),
    val totalByType: List<TypeAmount> = emptyList(),
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val timeUnit: StatisticTimeUnit = StatisticTimeUnit.MONTH
)

@HiltViewModel
class BillViewModel @Inject constructor(
    private val repository: BillRepository
) : ViewModel() {
    val bills: StateFlow<List<BillEntity>> = repository.getAllBills()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // 统计数据状态
    private val _statisticsState = MutableStateFlow(StatisticsState())
    val statisticsState: StateFlow<StatisticsState> = _statisticsState.asStateFlow()
    
    init {
        // 初始化时加载当前月份的统计数据
        loadStatistics()
    }
    
    /**
     * 插入新的账单记录
     */
    suspend fun insertBill(bill: BillEntity) {
        repository.insertBill(bill)
        // 插入新账单后刷新统计数据
        loadStatistics()
    }
    
    /**
     * 删除指定ID的账单
     */
    fun deleteBill(billId: Int) {
        viewModelScope.launch {
            repository.deleteBillById(billId)
            // 删除账单后刷新统计数据
            loadStatistics()
        }
    }
    
    /**
     * 加载统计数据
     */
    fun loadStatistics() {
        viewModelScope.launch {
            _statisticsState.update { it.copy(isLoading = true) }
            
            try {
                val state = _statisticsState.value
                val year = state.year
                val month = state.month
                val timeUnit = state.timeUnit
                
                when (timeUnit) {
                    StatisticTimeUnit.MONTH -> loadMonthlyStatistics(year, month)
                    StatisticTimeUnit.YEAR -> loadYearlyStatistics(year)
                }
            } finally {
                _statisticsState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * 设置统计的时间单位
     */
    fun setTimeUnit(timeUnit: StatisticTimeUnit) {
        _statisticsState.update { it.copy(timeUnit = timeUnit) }
        loadStatistics()
    }
    
    /**
     * 设置统计的年份
     */
    fun setYear(year: Int) {
        _statisticsState.update { it.copy(year = year) }
        loadStatistics()
    }
    
    /**
     * 设置统计的月份
     */
    fun setMonth(month: Int) {
        if (month in 1..12) {
            _statisticsState.update { it.copy(month = month) }
            loadStatistics()
        }
    }
    
    /**
     * 设置下一个月份
     */
    fun nextMonth() {
        val currentState = _statisticsState.value
        val calendar = Calendar.getInstance()
        calendar.set(currentState.year, currentState.month - 1, 1)
        calendar.add(Calendar.MONTH, 1)
        
        _statisticsState.update { 
            it.copy(
                year = calendar.get(Calendar.YEAR), 
                month = calendar.get(Calendar.MONTH) + 1
            ) 
        }
        loadStatistics()
    }
    
    /**
     * 设置上一个月份
     */
    fun previousMonth() {
        val currentState = _statisticsState.value
        val calendar = Calendar.getInstance()
        calendar.set(currentState.year, currentState.month - 1, 1)
        calendar.add(Calendar.MONTH, -1)
        
        _statisticsState.update { 
            it.copy(
                year = calendar.get(Calendar.YEAR), 
                month = calendar.get(Calendar.MONTH) + 1
            ) 
        }
        loadStatistics()
    }
    
    /**
     * 设置下一年
     */
    fun nextYear() {
        _statisticsState.update { it.copy(year = it.year + 1) }
        loadStatistics()
    }
    
    /**
     * 设置上一年
     */
    fun previousYear() {
        _statisticsState.update { it.copy(year = it.year - 1) }
        loadStatistics()
    }
    
    /**
     * 加载月度统计数据
     */
    private suspend fun loadMonthlyStatistics(year: Int, month: Int) {
        val expensesByCategory = repository.getMonthlyExpensesByCategory(year, month)
        val incomesByCategory = repository.getMonthlyIncomesByCategory(year, month)
        val totalByType = repository.getMonthlyTotalByType(year, month)
        
        val totalExpense = totalByType.find { it.type == "支出" }?.amount ?: 0.0
        val totalIncome = totalByType.find { it.type == "收入" }?.amount ?: 0.0
        
        _statisticsState.update { 
            it.copy(
                expensesByCategory = expensesByCategory,
                incomesByCategory = incomesByCategory,
                totalByType = totalByType,
                totalExpense = totalExpense,
                totalIncome = totalIncome
            ) 
        }
    }
    
    /**
     * 加载年度统计数据
     */
    private suspend fun loadYearlyStatistics(year: Int) {
        val expensesByCategory = repository.getYearlyExpensesByCategory(year)
        val incomesByCategory = repository.getYearlyIncomesByCategory(year)
        val totalByType = repository.getYearlyTotalByType(year)
        
        val totalExpense = totalByType.find { it.type == "支出" }?.amount ?: 0.0
        val totalIncome = totalByType.find { it.type == "收入" }?.amount ?: 0.0
        
        _statisticsState.update { 
            it.copy(
                expensesByCategory = expensesByCategory,
                incomesByCategory = incomesByCategory,
                totalByType = totalByType,
                totalExpense = totalExpense,
                totalIncome = totalIncome
            ) 
        }
    }
} 