package com.sangeetmind.core.database

import android.content.Context
import androidx.room.Room
import com.sangeetmind.core.database.dao.RaagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SangeetMindDatabase {
        return Room.databaseBuilder(
            context,
            SangeetMindDatabase::class.java,
            "sangeetmind_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideRaagDao(database: SangeetMindDatabase): RaagDao {
        return database.raagDao()
    }
}

