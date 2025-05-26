package com.example.monay.data.model

import java.time.LocalDateTime

data class Bill(
    val id: Long = 0,
    val amount: Double,
    val type: BillType,
    val category: String,
    val note: String,
    val date: LocalDateTime
) 