package com.dailypulse.util

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * 🌟 THE DATA TRANSLATOR
 * Room only knows how to store simple types (String, Int, etc.).
 * These converters allow it to store complex types like Lists if needed.
 */
class Converters {
    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, String::class.java)
    private val adapter = moshi.adapter<List<String>>(listType)

    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        return value?.let { adapter.fromJson(it) }
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        return list?.let { adapter.toJson(it) }
    }
}