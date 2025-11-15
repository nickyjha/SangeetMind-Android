package com.sangeetmind.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sangeetmind.core.database.dao.RaagDao
import com.sangeetmind.core.database.entity.RaagEntity

@Database(
    entities = [RaagEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SangeetMindDatabase : RoomDatabase() {
    abstract fun raagDao(): RaagDao
}

