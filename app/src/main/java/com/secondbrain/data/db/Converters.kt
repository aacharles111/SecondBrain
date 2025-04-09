package com.secondbrain.data.db

import androidx.room.TypeConverter
import com.secondbrain.data.model.CardType
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

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isBlank()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromCardType(value: CardType): String {
        return value.name
    }

    @TypeConverter
    fun toCardType(value: String): CardType {
        return try {
            CardType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            CardType.NOTE // Default value
        }
    }
}
