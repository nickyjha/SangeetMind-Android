package com.sangeetmind.core.database

import androidx.room.TypeConverter
import com.sangeetmind.libs.models.Intensity
import com.sangeetmind.libs.models.Mood
import com.sangeetmind.libs.models.TimeOfDay

class Converters {
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun fromTimeOfDay(value: TimeOfDay?): String? {
        return value?.name
    }

    @TypeConverter
    fun toTimeOfDay(value: String?): TimeOfDay? {
        return value?.let { TimeOfDay.valueOf(it) }
    }

    @TypeConverter
    fun fromMood(value: Mood?): String? {
        return value?.name
    }

    @TypeConverter
    fun toMood(value: String?): Mood? {
        return value?.let { Mood.valueOf(it) }
    }

    @TypeConverter
    fun fromIntensity(value: Intensity?): String? {
        return value?.name
    }

    @TypeConverter
    fun toIntensity(value: String?): Intensity? {
        return value?.let { Intensity.valueOf(it) }
    }
}

