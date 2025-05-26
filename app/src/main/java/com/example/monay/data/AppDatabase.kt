package com.example.monay.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AccountEntity::class, BillEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao
} 