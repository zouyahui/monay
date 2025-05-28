package com.example.monay.data

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Room 数据库类型转换器
 * 用于在数据库和应用之间转换复杂类型
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it / 1000, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)?.times(1000)
    }
} 