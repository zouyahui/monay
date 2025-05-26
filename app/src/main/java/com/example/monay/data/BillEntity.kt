package com.example.monay.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.monay.data.model.Bill
import com.example.monay.data.model.BillType
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long = 1, // 默认账户ID
    val type: String,        // 使用字符串存储枚举值
    val category: String,
    val amount: Double,
    val time: Long,         // 使用时间戳存储日期
    val remark: String? = null
) {
    companion object {
        fun fromBill(bill: Bill): BillEntity {
            return BillEntity(
                id = bill.id,
                type = bill.type.name,
                category = bill.category,
                amount = bill.amount,
                time = bill.date.toEpochSecond(ZoneOffset.UTC),
                remark = bill.note
            )
        }
    }

    fun toBill(): Bill {
        return Bill(
            id = id,
            type = BillType.valueOf(type),
            category = category,
            amount = amount,
            date = LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC),
            note = remark ?: ""
        )
    }
} 