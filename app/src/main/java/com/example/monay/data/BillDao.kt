package com.example.monay.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity): Long

    @Query("SELECT * FROM bills ORDER BY time DESC")
    fun getAllBills(): Flow<List<BillEntity>>
    
    /**
     * 删除指定ID的账单
     * @param billId 要删除的账单ID
     */
    @Query("DELETE FROM bills WHERE id = :billId")
    suspend fun deleteBillById(billId: Int)
    
    /**
     * 按类别统计指定月份的支出金额
     * @param startTime 月份开始时间戳
     * @param endTime 月份结束时间戳
     * @return 每个类别的支出金额列表
     */
    @Query("SELECT category, SUM(amount) as amount FROM bills WHERE type = '支出' AND time BETWEEN :startTime AND :endTime GROUP BY category ORDER BY amount DESC")
    suspend fun getMonthlyExpensesByCategory(startTime: Long, endTime: Long): List<CategoryAmount>
    
    /**
     * 按类别统计指定月份的收入金额
     * @param startTime 月份开始时间戳
     * @param endTime 月份结束时间戳
     * @return 每个类别的收入金额列表
     */
    @Query("SELECT category, SUM(amount) as amount FROM bills WHERE type = '收入' AND time BETWEEN :startTime AND :endTime GROUP BY category ORDER BY amount DESC")
    suspend fun getMonthlyIncomesByCategory(startTime: Long, endTime: Long): List<CategoryAmount>
    
    /**
     * 按类别统计指定年份的支出金额
     * @param startTime 年份开始时间戳
     * @param endTime 年份结束时间戳
     * @return 每个类别的支出金额列表
     */
    @Query("SELECT category, SUM(amount) as amount FROM bills WHERE type = '支出' AND time BETWEEN :startTime AND :endTime GROUP BY category ORDER BY amount DESC")
    suspend fun getYearlyExpensesByCategory(startTime: Long, endTime: Long): List<CategoryAmount>
    
    /**
     * 按类别统计指定年份的收入金额
     * @param startTime 年份开始时间戳
     * @param endTime 年份结束时间戳
     * @return 每个类别的收入金额列表
     */
    @Query("SELECT category, SUM(amount) as amount FROM bills WHERE type = '收入' AND time BETWEEN :startTime AND :endTime GROUP BY category ORDER BY amount DESC")
    suspend fun getYearlyIncomesByCategory(startTime: Long, endTime: Long): List<CategoryAmount>
    
    /**
     * 获取指定月份的总收入和总支出
     * @param startTime 月份开始时间戳
     * @param endTime 月份结束时间戳
     * @return 每种类型的总金额
     */
    @Query("SELECT type, SUM(amount) as amount FROM bills WHERE time BETWEEN :startTime AND :endTime GROUP BY type")
    suspend fun getMonthlyTotalByType(startTime: Long, endTime: Long): List<TypeAmount>
    
    /**
     * 获取指定年份的总收入和总支出
     * @param startTime 年份开始时间戳
     * @param endTime 年份结束时间戳
     * @return 每种类型的总金额
     */
    @Query("SELECT type, SUM(amount) as amount FROM bills WHERE time BETWEEN :startTime AND :endTime GROUP BY type")
    suspend fun getYearlyTotalByType(startTime: Long, endTime: Long): List<TypeAmount>
} 