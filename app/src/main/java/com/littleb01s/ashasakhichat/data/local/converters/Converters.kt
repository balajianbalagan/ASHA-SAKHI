package com.littleb01s.ashasakhichat.data.local.converters

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Add more type converters as needed for complex data types
    // Examples might include:
    // - List<String> to String and vice versa
    // - Custom enums to String and vice versa
    // - JSON objects to String and vice versa
} 