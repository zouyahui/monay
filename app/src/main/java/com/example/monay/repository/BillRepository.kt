package com.example.monay.repository

import com.example.monay.data.*
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject

class BillRepository @Inject constructor(private val billDao: BillDao) {
    suspend fun insertBill(bill: BillEntity): Long = billDao.insertBill(bill)
    fun getAllBills(): Flow<List<BillEntity>> = billDao.getAllBills()
    
    /**
     * 删除指定ID的账单
     */
    suspend fun deleteBillById(billId: Int) = billDao.deleteBillById(billId)
    
    /**
     * 获取指定月份的支出分类统计
     */
    suspend fun getMonthlyExpensesByCategory(year: Int, month: Int): List<CategoryAmount> {
        val timeRange = getMonthTimeRange(year, month)
        return billDao.getMonthlyExpensesByCategory(timeRange.startTime, timeRange.endTime)
    }
    
    /**
     * 获取指定月份的收入分类统计
     */
    suspend fun getMonthlyIncomesByCategory(year: Int, month: Int): List<CategoryAmount> {
        val timeRange = getMonthTimeRange(year, month)
        return billDao.getMonthlyIncomesByCategory(timeRange.startTime, timeRange.endTime)
    }
    
    /**
     * 获取指定年份的支出分类统计
     */
    suspend fun getYearlyExpensesByCategory(year: Int): List<CategoryAmount> {
        val timeRange = getYearTimeRange(year)
        return billDao.getYearlyExpensesByCategory(timeRange.startTime, timeRange.endTime)
    }
    
    /**
     * 获取指定年份的收入分类统计
     */
    suspend fun getYearlyIncomesByCategory(year: Int): List<CategoryAmount> {
        val timeRange = getYearTimeRange(year)
        return billDao.getYearlyIncomesByCategory(timeRange.startTime, timeRange.endTime)
    }
    
    /**
     * 获取指定月份的总收支统计
     */
    suspend fun getMonthlyTotalByType(year: Int, month: Int): List<TypeAmount> {
        val timeRange = getMonthTimeRange(year, month)
        return billDao.getMonthlyTotalByType(timeRange.startTime, timeRange.endTime)
    }
    
    /**
     * 获取指定年份的总收支统计
     */
    suspend fun getYearlyTotalByType(year: Int): List<TypeAmount> {
        val timeRange = getYearTimeRange(year)
        return billDao.getYearlyTotalByType(timeRange.startTime, timeRange.endTime)
    }
    
    /**
     * 获取指定月份的时间范围
     */
    private fun getMonthTimeRange(year: Int, month: Int): TimeRange {
        val calendar = Calendar.getInstance()
        
        // 设置为月初
        calendar.set(year, month - 1, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        
        // 设置为月末
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTime = calendar.timeInMillis
        
        return TimeRange(startTime, endTime)
    }
    
    /**
     * 获取指定年份的时间范围
     */
    private fun getYearTimeRange(year: Int): TimeRange {
        val calendar = Calendar.getInstance()
        
        // 设置为年初
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        
        // 设置为年末
        calendar.set(Calendar.MONTH, Calendar.DECEMBER)
        calendar.set(Calendar.DAY_OF_MONTH, 31)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTime = calendar.timeInMillis
        
        return TimeRange(startTime, endTime)
    }
}