package com.example.monay.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountId: Int,
    val type: String, // 收入/支出/转账
    val category: String,
    val amount: Double,
    val time: Long,
    val remark: String?
) 