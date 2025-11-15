package com.sangeetmind.core.database.dao

import androidx.room.*
import com.sangeetmind.core.database.entity.RaagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RaagDao {
    @Query("SELECT * FROM raags")
    fun getAllRaags(): Flow<List<RaagEntity>>

    @Query("SELECT * FROM raags WHERE id = :id")
    suspend fun getRaagById(id: String): RaagEntity?

    @Query("SELECT * FROM raags WHERE isFavorite = 1")
    fun getFavoriteRaags(): Flow<List<RaagEntity>>

    @Query("SELECT * FROM raags WHERE isDownloaded = 1")
    fun getDownloadedRaags(): Flow<List<RaagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaag(raag: RaagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaags(raags: List<RaagEntity>)

    @Update
    suspend fun updateRaag(raag: RaagEntity)

    @Query("UPDATE raags SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("UPDATE raags SET isDownloaded = :isDownloaded, localAudioPath = :localPath WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean, localPath: String?)

    @Delete
    suspend fun deleteRaag(raag: RaagEntity)

    @Query("DELETE FROM raags")
    suspend fun deleteAllRaags()
}

